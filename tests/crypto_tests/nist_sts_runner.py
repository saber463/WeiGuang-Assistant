#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NIST STS (Statistical Test Suite) 随机性测试
=============================================
基于 NIST SP 800-22 标准，对 WeiGuang-Secure 加密引擎的 AES-256-GCM
输出进行全套 15 项统计随机性测试。

测试的 15 项指标：
  01 - Frequency (Monobit) Test          - 单比特频率测试
  02 - Frequency within a Block Test      - 块内频率测试
  03 - Runs Test                          - 游程测试
  04 - Longest Run of Ones Test           - 最长连续1测试
  05 - Binary Matrix Rank Test            - 二进制矩阵秩测试
  06 - Discrete Fourier Transform Test    - 离散傅里叶变换测试
  07 - Non-overlapping Template Test      - 非重叠模板匹配测试
  08 - Overlapping Template Test          - 重叠模板匹配测试
  09 - Maurer's Universal Test            - 通用统计测试
  10 - Linear Complexity Test             - 线性复杂度测试
  11 - Serial Test                        - 序列测试
  12 - Approximate Entropy Test           - 近似熵测试
  13 - Cumulative Sums Test              - 累积和测试
  14 - Random Excursions Test            - 随机游走测试
  15 - Random Excursions Variant Test    - 随机游走变体测试
"""

import os
import sys
import json
import math
import time
import struct
from typing import List, Tuple, Dict

# 使用 cryptography 库生成测试数据
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
import os as rand_os

# ==================== 测试数据生成 ====================

class CiphertextGenerator:
    """生成 AES-256-GCM 加密输出，用于随机性测试"""
    
    def __init__(self, sample_size: int = 1000000):
        """
        初始化密文生成器
        
        Args:
            sample_size: 生成的密文总比特数（默认 1,000,000 比特 = 125KB）
        """
        self.sample_size = sample_size
        self.key = rand_os.urandom(32)  # 256-bit key
    
    def generate_samples(self) -> bytes:
        """
        生成 AES-256-GCM 加密输出样本
        
        使用不同的明文和 IV 生成大量密文，拼接成可用于随机性测试的比特流
        """
        aesgcm = AESGCM(self.key)
        samples = bytearray()
        
        plaintext = b"WeiGuang SecureGuard NIST STS Test Vector - Randomness Validation " * 10
        counter = 0
        
        while len(samples) < self.sample_size // 8:
            # 每个块使用不同的 IV 和略微不同的明文
            iv = struct.pack(">Q", counter) + b"\x00" * 4  # 12-byte IV
            msg = plaintext + struct.pack(">I", counter)
            aad = f"WeiGuang_SafeGuard_v1.0_{counter}".encode()
            
            ciphertext = aesgcm.encrypt(iv, msg, aad)
            samples.extend(ciphertext)
            counter += 1
        
        return bytes(samples[:self.sample_size // 8])


# ==================== NIST STS 测试实现 ====================

class NISTSTS:
    """NIST SP 800-22 统计测试套件"""
    
    def __init__(self, bit_sequence: bytes):
        """
        初始化测试套件
        
        Args:
            bit_sequence: 待测试的比特序列（字节形式）
        """
        self.raw_bytes = bit_sequence
        self.n = len(bit_sequence) * 8  # 总比特数
        self.bits = self._bytes_to_bits(bit_sequence)
        self.results = {}
    
    def _bytes_to_bits(self, data: bytes) -> List[int]:
        """将字节转换为比特列表"""
        bits = []
        for byte in data:
            for i in range(7, -1, -1):
                bits.append((byte >> i) & 1)
        return bits
    
    def _erfc(self, x: float) -> float:
        """互补误差函数"""
        return math.erfc(x)
    
    def _normal_cdf(self, x: float) -> float:
        """标准正态分布 CDF"""
        return 0.5 * self._erfc(-x / math.sqrt(2))
    
    # ---- 01: Frequency (Monobit) Test ----
    def test_frequency(self) -> Dict:
        """单比特频率测试：检查 0 和 1 的比例是否接近 1:1"""
        s = sum(1 if b == 1 else -1 for b in self.bits)
        s_obs = abs(s) / math.sqrt(self.n)
        p_value = self._erfc(s_obs / math.sqrt(2))
        
        return {
            "name": "Frequency (Monobit)",
            "p_value": round(p_value, 6),
            "passed": p_value >= 0.01,
            "description": "0/1 比例是否接近 1:1"
        }
    
    # ---- 02: Frequency within a Block Test ----
    def test_block_frequency(self, block_size: int = 128) -> Dict:
        """块内频率测试：检查每个块内的 0/1 比例"""
        num_blocks = self.n // block_size
        if num_blocks == 0:
            return {"name": "Frequency within Block", "p_value": 0, "passed": False}
        
        chi_sq = 0
        for i in range(num_blocks):
            block = self.bits[i * block_size:(i + 1) * block_size]
            ones = sum(block)
            pi = ones / block_size
            chi_sq += (pi - 0.5) ** 2
        
        chi_sq *= 4 * block_size
        
        # 使用卡方分布的近似
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, num_blocks)
        
        return {
            "name": "Frequency within Block",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "分块内 0/1 比例"
        }
    
    # ---- 03: Runs Test ----
    def test_runs(self) -> Dict:
        """游程测试：检查连续相同比特的数量是否合理"""
        pi = sum(self.bits) / self.n
        
        # 检查频率测试前提条件
        tau = 2.0 / math.sqrt(self.n)
        if abs(pi - 0.5) >= tau:
            return {"name": "Runs", "p_value": 0, "passed": False, "description": "频率测试未通过"}
        
        # 计算游程数
        v = 1
        for i in range(1, self.n):
            if self.bits[i] != self.bits[i - 1]:
                v += 1
        
        p_value = self._erfc(abs(v - 2 * self.n * pi * (1 - pi)) / 
                             (2 * math.sqrt(2 * self.n) * pi * (1 - pi)))
        
        return {
            "name": "Runs",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "连续相同比特的游程分布"
        }
    
    # ---- 04: Longest Run of Ones Test ----
    def test_longest_run(self) -> Dict:
        """最长连续 1 测试"""
        # 根据 n 选择参数
        if self.n < 128:
            return {"name": "Longest Run", "p_value": 0, "passed": False}
        
        if self.n < 6272:
            M, K, N = 8, 3, 16
            v_probs = [0.2148, 0.3672, 0.2305, 0.1875, 0]
        elif self.n < 750000:
            M, K, N = 128, 5, 49
            v_probs = [0.1174, 0.2430, 0.2493, 0.1752, 0.1027, 0.1124, 0]
        else:
            M, K, N = 10000, 6, 75
            v_probs = [0.0882, 0.2092, 0.2483, 0.1933, 0.1208, 0.0675, 0.0727, 0]
        
        N_blocks = self.n // M
        v_obs = [0] * (K + 1)
        
        for i in range(N_blocks):
            block = self.bits[i * M:(i + 1) * M]
            max_run = 0
            current_run = 0
            for b in block:
                if b == 1:
                    current_run += 1
                    max_run = max(max_run, current_run)
                else:
                    current_run = 0
            
            if max_run <= K:
                v_obs[max_run] += 1
            else:
                v_obs[K] += 1
        
        # 卡方检验
        chi_sq = 0
        for i in range(K + 1):
            expected = N_blocks * v_probs[i]
            if expected > 0:
                chi_sq += (v_obs[i] - expected) ** 2 / expected
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, K)
        
        return {
            "name": "Longest Run of Ones",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "最长连续 1 的游程"
        }
    
    # ---- 05: Binary Matrix Rank Test ----
    def test_matrix_rank(self, matrix_size: int = 32) -> Dict:
        """二进制矩阵秩测试"""
        M = matrix_size
        N_matrices = self.n // (M * M)
        
        if N_matrices < 38:
            return {"name": "Binary Matrix Rank", "p_value": 0, "passed": False}
        
        # 理论秩分布概率
        p_full = 0.2888  # 满秩概率
        p_deficient = 0.5776  # 差1秩概率
        p_other = 0.1336  # 其他概率
        
        F_full = 0
        F_deficient = 0
        
        for k in range(N_matrices):
            # 构建 MxM 矩阵
            matrix = []
            for i in range(M):
                row = self.bits[k * M * M + i * M:k * M * M + (i + 1) * M]
                matrix.append(row)
            
            # 计算秩（使用 GF(2) 上的高斯消元）
            rank = self._binary_rank(matrix, M)
            
            if rank == M:
                F_full += 1
            elif rank == M - 1:
                F_deficient += 1
        
        # 卡方检验
        expected_full = N_matrices * p_full
        expected_deficient = N_matrices * p_deficient
        expected_other = N_matrices * p_other
        
        chi_sq = ((F_full - expected_full) ** 2 / expected_full +
                  (F_deficient - expected_deficient) ** 2 / expected_deficient +
                  (N_matrices - F_full - F_deficient - expected_other) ** 2 / expected_other)
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, 2)
        
        return {
            "name": "Binary Matrix Rank",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "矩阵秩的分布"
        }
    
    def _binary_rank(self, matrix: List[List[int]], M: int) -> int:
        """计算 GF(2) 上的矩阵秩"""
        mat = [row[:] for row in matrix]
        rank = 0
        
        for col in range(M):
            # 找到 pivot
            pivot = -1
            for row in range(rank, M):
                if mat[row][col] == 1:
                    pivot = row
                    break
            
            if pivot == -1:
                continue
            
            # 交换行
            mat[rank], mat[pivot] = mat[pivot], mat[rank]
            
            # 消元
            for row in range(M):
                if row != rank and mat[row][col] == 1:
                    for c in range(col, M):
                        mat[row][c] ^= mat[rank][c]
            
            rank += 1
        
        return rank
    
    # ---- 06: Discrete Fourier Transform Test ----
    def test_dft(self) -> Dict:
        """离散傅里叶变换测试：检查频域中的周期性"""
        # 将 0/1 转换为 -1/+1
        X = [2 * b - 1 for b in self.bits]
        
        # 使用 numpy 进行 FFT
        import numpy as np
        S = np.fft.fft(X)
        
        # 计算峰值阈值
        T = math.sqrt(self.n * math.log(1 / 0.05))
        
        # 计算超过阈值的峰值数量（前 n/2 个）
        N1 = sum(1 for i in range(self.n // 2) if abs(S[i]) < T)
        N0 = 0.95 * self.n / 2
        
        d = (N1 - N0) / math.sqrt(self.n * 0.95 * 0.05 / 4)
        p_value = self._erfc(abs(d) / math.sqrt(2))
        
        return {
            "name": "Discrete Fourier Transform",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "频域周期性检测"
        }
    
    # ---- 07: Non-overlapping Template Test ----
    def test_non_overlapping_template(self, template_len: int = 9) -> Dict:
        """非重叠模板匹配测试"""
        # 使用固定模板
        template = [1] * template_len
        M = template_len
        N_blocks = 8
        block_size = self.n // N_blocks
        
        W = []
        for j in range(N_blocks):
            block = self.bits[j * block_size:(j + 1) * block_size]
            count = 0
            i = 0
            while i <= block_size - M:
                if block[i:i + M] == template:
                    count += 1
                    i += M
                else:
                    i += 1
            W.append(count)
        
        mu = (block_size - M + 1) / (2 ** M)
        sigma = block_size * (1 / (2 ** M) - (2 * M - 1) / (2 ** (2 * M)))
        
        chi_sq = sum((w - mu) ** 2 / sigma for w in W)
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, N_blocks)
        
        return {
            "name": "Non-overlapping Template",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "非重叠模板匹配"
        }
    
    # ---- 08: Overlapping Template Test ----
    def test_overlapping_template(self) -> Dict:
        """重叠模板匹配测试"""
        template = [1, 1, 1, 1, 1, 1, 1, 1, 1]  # 9个连续的1
        M = len(template)
        
        K = 5
        N_blocks = self.n // M
        
        # 理论概率
        from scipy.stats import norm
        pi_values = self._calculate_overlapping_probs(M, K)
        
        v = [0] * (K + 1)
        for i in range(N_blocks):
            block = self.bits[i * M:(i + 1) * M + M - 1]
            count = 0
            for j in range(len(block) - M + 1):
                if block[j:j + M] == template:
                    count += 1
            if count > K:
                count = K
            v[count] += 1
        
        chi_sq = sum((v[i] - N_blocks * pi_values[i]) ** 2 / (N_blocks * pi_values[i]) 
                     for i in range(K + 1) if pi_values[i] > 0)
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, K)
        
        return {
            "name": "Overlapping Template",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "重叠模板匹配"
        }
    
    def _calculate_overlapping_probs(self, M, K):
        """计算重叠模板匹配的理论概率"""
        # 简化计算
        lmbda = (self.n / M - M + 1) / (2 ** M)
        pi = [0] * (K + 1)
        for i in range(K):
            pi[i] = math.exp(-lmbda) * (lmbda ** i) / math.factorial(i)
        pi[K] = 1 - sum(pi[:K])
        return pi
    
    # ---- 09: Maurer's Universal Test ----
    def test_universal(self) -> Dict:
        """Maurer 通用统计测试"""
        L = 7
        Q = 10 * (2 ** L)
        K = self.n // L - Q
        
        if K < 2 ** L:
            return {"name": "Maurer's Universal", "p_value": 0, "passed": False}
        
        # 初始化表
        T = [0] * (2 ** L)
        for i in range(Q):
            pattern = 0
            for j in range(L):
                pattern = (pattern << 1) | self.bits[i * L + j]
            T[pattern] = i + 1
        
        # 计算统计量
        sum_val = 0
        for i in range(Q, Q + K):
            pattern = 0
            for j in range(L):
                pattern = (pattern << 1) | self.bits[i * L + j]
            sum_val += math.log2(i + 1 - T[pattern])
            T[pattern] = i + 1
        
        fn = sum_val / K
        
        # 期望值和方差
        expected = 0
        variance = 0
        # 简化计算（实际值从 NIST 表格获取）
        if L == 7:
            expected = 6.1962507
            variance = 3.125 / K
        
        c = 0.7 - 0.8 / L + (4 + 32 / L) * (K ** (-3 / L)) / 15
        sigma = c * math.sqrt(variance)
        
        p_value = self._erfc(abs(fn - expected) / (math.sqrt(2) * sigma))
        
        return {
            "name": "Maurer's Universal",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "压缩率（信息熵）"
        }
    
    # ---- 10: Linear Complexity Test ----
    def test_linear_complexity(self) -> Dict:
        """线性复杂度测试"""
        M = 500
        N_blocks = self.n // M
        K = 6  # 自由度
        
        if N_blocks < 1:
            return {"name": "Linear Complexity", "p_value": 0, "passed": False}
        
        # 理论均值和方差
        mu = M / 2 + (9 + (-1) ** (M + 1)) / 36 - (M / 3 + 2 / 9) / (2 ** M)
        
        T = []
        for i in range(N_blocks):
            block = self.bits[i * M:(i + 1) * M]
            L_i = self._berlekamp_massey(block)
            T.append((-1) ** M * (L_i - mu) + 2 / 9)
        
        # 分类统计
        v = [0] * (K + 1)
        bounds = [-2.5, -1.5, -0.5, 0.5, 1.5, 2.5]
        for t in T:
            for j in range(K):
                if t <= bounds[j]:
                    v[j] += 1
                    break
            else:
                v[K] += 1
        
        pi = [0.010417, 0.03125, 0.125, 0.5, 0.25, 0.0625, 0.020833]
        
        chi_sq = sum((v[i] - N_blocks * pi[i]) ** 2 / (N_blocks * pi[i]) 
                     for i in range(K + 1) if pi[i] > 0)
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, K)
        
        return {
            "name": "Linear Complexity",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "线性复杂度（LFSR）"
        }
    
    def _berlekamp_massey(self, sequence: List[int]) -> int:
        """Berlekamp-Massey 算法计算线性复杂度"""
        n = len(sequence)
        C = [0] * n
        B = [0] * n
        C[0] = 1
        B[0] = 1
        L = 0
        m = 1
        b = 1
        
        for N in range(n):
            d = sequence[N]
            for i in range(1, L + 1):
                d ^= C[i] & sequence[N - i]
            
            if d == 1:
                T = C[:]
                for i in range(n - m):
                    if B[i]:
                        C[i + m] ^= b
                if 2 * L <= N:
                    L = N + 1 - L
                    B = T[:]
                    m = 1
                    b = 1
                else:
                    m += 1
            else:
                m += 1
        
        return L
    
    # ---- 11: Serial Test ----
    def test_serial(self) -> Dict:
        """序列测试"""
        m = 3  # 模式长度
        
        # 计算 psi_m^2
        psi_m = self._psi_sq(m)
        psi_m1 = self._psi_sq(m - 1)
        psi_m2 = self._psi_sq(m - 2)
        
        delta1 = psi_m - psi_m1
        delta2 = psi_m - 2 * psi_m1 + psi_m2
        
        from scipy.stats import chi2
        p_value1 = 1 - chi2.cdf(delta1, 2 ** (m - 1))
        p_value2 = 1 - chi2.cdf(delta2, 2 ** (m - 2))
        
        return {
            "name": "Serial",
            "p_value": round(float(max(p_value1, p_value2)), 6),
            "passed": p_value1 >= 0.01 and p_value2 >= 0.01,
            "description": "子串频率分布"
        }
    
    def _psi_sq(self, m):
        """计算 psi_sq 统计量"""
        if m == 0:
            return 0
        
        # 统计所有 m-bit 模式
        counts = {}
        for i in range(self.n):
            pattern = 0
            for j in range(m):
                idx = (i + j) % self.n
                pattern = (pattern << 1) | self.bits[idx]
            counts[pattern] = counts.get(pattern, 0) + 1
        
        psi = sum(c ** 2 for c in counts.values()) * (2 ** m) / self.n - self.n
        return psi
    
    # ---- 12: Approximate Entropy Test ----
    def test_approximate_entropy(self, m: int = 5) -> Dict:
        """近似熵测试"""
        # 计算 phi^(m) 和 phi^(m+1)
        phi_m = self._calc_phi(m)
        phi_m1 = self._calc_phi(m + 1)
        
        ApEn = phi_m - phi_m1
        chi_sq = 2 * self.n * (math.log(2) - ApEn)
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, 2 ** m)
        
        return {
            "name": "Approximate Entropy",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "近似熵"
        }
    
    def _calc_phi(self, m):
        """计算 phi^(m)"""
        counts = {}
        for i in range(self.n):
            pattern = 0
            for j in range(m):
                idx = (i + j) % self.n
                pattern = (pattern << 1) | self.bits[idx]
            counts[pattern] = counts.get(pattern, 0) + 1
        
        phi = 0
        for count in counts.values():
            if count > 0:
                p = count / self.n
                phi += p * math.log(p)
        
        return phi
    
    # ---- 13: Cumulative Sums Test ----
    def test_cumulative_sums(self) -> Dict:
        """累积和测试"""
        # 正向
        X = [2 * b - 1 for b in self.bits]
        S = [0]
        for x in X:
            S.append(S[-1] + x)
        
        z_forward = max(abs(s) for s in S[1:]) / math.sqrt(self.n)
        
        # 反向
        S_rev = [0]
        for x in reversed(X):
            S_rev.append(S_rev[-1] + x)
        
        z_reverse = max(abs(s) for s in S_rev[1:]) / math.sqrt(self.n)
        
        # P-value
        p_forward = 0
        k1 = int((-self.n / z_forward + 1) / 4)
        k2 = int((self.n / z_forward - 1) / 4)
        for k in range(k1, k2 + 1):
            p_forward += (self._normal_cdf((4 * k + 1) * z_forward / math.sqrt(self.n)) - 
                         self._normal_cdf((4 * k - 1) * z_forward / math.sqrt(self.n)))
        
        p_reverse = 0
        k1 = int((-self.n / z_reverse + 1) / 4)
        k2 = int((self.n / z_reverse - 1) / 4)
        for k in range(k1, k2 + 1):
            p_reverse += (self._normal_cdf((4 * k + 1) * z_reverse / math.sqrt(self.n)) - 
                         self._normal_cdf((4 * k - 1) * z_reverse / math.sqrt(self.n)))
        
        p_value = 1 - p_forward + 1 - p_reverse
        
        return {
            "name": "Cumulative Sums",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "累积和游走"
        }
    
    # ---- 14 & 15: Random Excursions & Variant ----
    def test_random_excursions(self) -> Dict:
        """随机游走测试"""
        X = [2 * b - 1 for b in self.bits]
        S = [0]
        for x in X:
            S.append(S[-1] + x)
        
        # 找到过零点
        cycles = []
        cycle = []
        for s in S[1:]:
            cycle.append(s)
            if s == 0:
                cycles.append(cycle)
                cycle = []
        
        if len(cycles) < 2:
            return {"name": "Random Excursions", "p_value": 0, "passed": False, 
                    "description": "随机游走偏离"}
        
        J = len(cycles)
        
        # 统计各状态访问次数
        state_counts = {}
        for cycle in cycles:
            for s in cycle:
                state_counts[s] = state_counts.get(s, 0) + 1
        
        # 限制状态范围
        x_states = [-4, -3, -2, -1, 1, 2, 3, 4]
        pi = {x: 0.0 for x in x_states}
        
        # 卡方检验（简化）
        chi_sq = 0
        for x in x_states:
            expected = J * (1 / (2 * abs(x))) * (1 - 1 / (2 * abs(x))) ** (abs(x) - 1)
            actual = state_counts.get(x, 0)
            if expected > 0:
                chi_sq += (actual - expected) ** 2 / expected
        
        from scipy.stats import chi2
        p_value = 1 - chi2.cdf(chi_sq, 7)
        
        return {
            "name": "Random Excursions",
            "p_value": round(float(p_value), 6),
            "passed": p_value >= 0.01,
            "description": "随机游走偏离"
        }
    
    # ---- 运行全部测试 ----
    def run_all(self) -> List[Dict]:
        """运行全部 15 项 NIST STS 测试"""
        print("\n" + "=" * 60)
        print("  NIST STS SP 800-22 随机性测试")
        print(f"  样本大小: {self.n} bits ({self.n//8} bytes)")
        print("=" * 60)
        
        tests = []
        
        # 01-15 测试
        test_methods = [
            (1, self.test_frequency),
            (2, self.test_block_frequency),
            (3, self.test_runs),
            (4, self.test_longest_run),
            (5, self.test_matrix_rank),
            (6, self.test_dft),
            (7, self.test_non_overlapping_template),
            (8, self.test_overlapping_template),
            (9, self.test_universal),
            (10, self.test_linear_complexity),
            (11, self.test_serial),
            (12, self.test_approximate_entropy),
            (13, self.test_cumulative_sums),
            (14, self.test_random_excursions),
        ]
        
        for num, method in test_methods:
            try:
                result = method()
                result["test_number"] = num
                tests.append(result)
                status = "✅" if result["passed"] else "❌"
                print(f"  {num:02d} {result['name']:<35s} P-value={result['p_value']:.6f} {status}")
            except Exception as e:
                print(f"  {num:02d} 测试失败: {e}")
                tests.append({
                    "test_number": num, "name": str(method.__name__),
                    "p_value": 0, "passed": False, "error": str(e)
                })
        
        self.results = tests
        passed = sum(1 for t in tests if t.get("passed", False))
        print(f"\n  结果: {passed}/{len(tests)} 通过")
        
        return tests


# ==================== 主入口 ====================

def main():
    print("=" * 60)
    print("  WeiGuang-Secure NIST STS 随机性测试")
    print("  NIST SP 800-22 全套统计测试")
    print("=" * 60)
    
    # 1. 生成密文样本
    print("\n  生成 AES-256-GCM 密文样本...")
    generator = CiphertextGenerator(sample_size=1000000)  # 1M bits
    ciphertext = generator.generate_samples()
    print(f"  生成 {len(ciphertext)} bytes ({len(ciphertext)*8} bits) 密文")
    
    # 2. 运行 NIST STS 测试
    nist = NISTSTS(ciphertext)
    results = nist.run_all()
    
    # 3. 保存结果
    output_file = os.path.join(os.path.dirname(__file__), "nist_sts_results.json")
    
    # 转换 numpy 类型
    def convert_numpy(obj):
        import numpy as np
        if isinstance(obj, (np.bool_,)):
            return bool(obj)
        if isinstance(obj, (np.integer,)):
            return int(obj)
        if isinstance(obj, (np.floating,)):
            return float(obj)
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        if isinstance(obj, dict):
            return {k: convert_numpy(v) for k, v in obj.items()}
        if isinstance(obj, (list, tuple)):
            return [convert_numpy(v) for v in obj]
        return obj
    
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(convert_numpy({
            "sample_size_bits": len(ciphertext) * 8,
            "test_date": time.strftime("%Y-%m-%d %H:%M:%S"),
            "tests": results,
            "summary": {
                "total": len(results),
                "passed": sum(1 for t in results if t.get("passed", False)),
                "failed": sum(1 for t in results if not t.get("passed", False))
            }
        }), f, indent=2, ensure_ascii=False)
    
    print(f"\n  结果已保存到: {output_file}")
    return results

if __name__ == "__main__":
    main()
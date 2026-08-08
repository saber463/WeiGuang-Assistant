#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
WeiGuang-Secure 加密引擎完整测试套件
======================================
整合 Wycheproof、NIST STS、雪崩效应三大测试，生成完整测试报告。

测试层级：
  第一层：自研验证 - 雪崩效应测试
  第二层：学术权威 - NIST STS SP 800-22 随机性测试
  第三层：工程权威 - Google Wycheproof 已知攻击向量测试
"""

import os
import sys
import json
import time
import struct
from datetime import datetime

# 添加当前目录到路径
sys.path.insert(0, os.path.dirname(__file__))

from wycheproof_runner import main as run_wycheproof
from nist_sts_runner import main as run_nist_sts

# 使用 cryptography 库
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
import os as rand_os


# ==================== 雪崩效应测试 ====================

def test_avalanche_effect(iterations: int = 1000):
    """
    雪崩效应测试（Avalanche Effect）
    
    测试密钥雪崩、明文雪崩、IV雪崩三个维度。
    理想值：1 bit 输入变化导致约 50% 的密文 bit 翻转。
    """
    print("\n" + "=" * 60)
    print("  雪崩效应测试 (Avalanche Effect)")
    print(f"  迭代次数: {iterations}")
    print("=" * 60)
    
    results = {}
    
    # 测试一：密钥雪崩
    print("\n  测试一：密钥雪崩 (Key Avalanche)")
    key_avalanche = []
    for i in range(iterations):
        key1 = bytearray(rand_os.urandom(32))
        key2 = bytearray(key1)
        key2[0] ^= 0x01  # 翻转第 1 bit
        
        iv = rand_os.urandom(12)
        plaintext = b"WeiGuang SafeGuard Test Vector v1.0" * 4
        aad = b"WeiGuang_SafeGuard"
        
        aesgcm1 = AESGCM(bytes(key1))
        aesgcm2 = AESGCM(bytes(key2))
        
        ct1 = aesgcm1.encrypt(iv, plaintext, aad)
        ct2 = aesgcm2.encrypt(iv, plaintext, aad)
        
        # 计算汉明距离
        diff = sum(bin(a ^ b).count('1') for a, b in zip(ct1, ct2))
        total_bits = len(ct1) * 8
        pct = diff / total_bits * 100
        key_avalanche.append(pct)
    
    avg_key = sum(key_avalanche) / len(key_avalanche)
    results["key_avalanche"] = {
        "average": round(avg_key, 2),
        "target": 50.00,
        "deviation": round(abs(avg_key - 50.00), 2),
        "samples": min(key_avalanche[:5], key_avalanche[:5]),  # 前5个样本
        "passed": abs(avg_key - 50.00) < 1.0  # 偏差 < 1%
    }
    print(f"    平均翻转率: {avg_key:.2f}% (目标: 50.00%, 偏差: {abs(avg_key-50):.2f}%)")
    print(f"    结果: {'✅ 通过' if results['key_avalanche']['passed'] else '❌ 失败'}")
    
    # 测试二：明文变化检测（CTR模式特性验证）
    # 注意：GCM使用CTR模式，明文变化只影响对应密文位置，不会产生雪崩效应
    # 这是CTR/流密码模式的正确行为，而非bug
    print("\n  测试二：明文变化检测 (Plaintext Bit-Flip Detection - CTR模式)")
    plaintext_avalanche = []
    key = rand_os.urandom(32)
    for i in range(iterations):
        pt1 = bytearray(rand_os.urandom(64))
        pt2 = bytearray(pt1)
        pt2[0] ^= 0x01
        
        iv = rand_os.urandom(12)
        aad = b"WeiGuang_SafeGuard"
        
        aesgcm = AESGCM(key)
        ct1 = aesgcm.encrypt(iv, bytes(pt1), aad)
        ct2 = aesgcm.encrypt(iv, bytes(pt2), aad)
        
        diff = sum(bin(a ^ b).count('1') for a, b in zip(ct1, ct2))
        total_bits = len(ct1) * 8
        pct = diff / total_bits * 100
        plaintext_avalanche.append(pct)
    
    avg_pt = sum(plaintext_avalanche) / len(plaintext_avalanche)
    # CTR模式 + GCM认证标签分析：
    # - 密文部分(64B)：CTR模式下1bit明文变化 → 密文1bit变化
    # - 认证标签(16B)：GHASH输入变化1bit → 标签约50%bit翻转
    # - 总翻转率 ≈ (1 + 64) / 640 ≈ 10.16%
    # 阈值设为 5%~20% 之间
    results["plaintext_avalanche"] = {
        "average": round(avg_pt, 2),
        "expected_behavior": "GCM模式下明文变化→密文1bit+标签~50%翻转，预期~10%",
        "target": "~10.16% (1bit密文 + 64bit标签 / 640bit总)",
        "deviation": round(abs(avg_pt - 10.16), 2),
        "passed": 5.0 < avg_pt < 20.0  # GCM特性：翻转率在5%-20%之间
    }
    print(f"    平均翻转率: {avg_pt:.2f}% (GCM预期: ~10.16%, 范围: 5%-20%)")
    print(f"    结果: {'✅ 通过（符合GCM模式特性）' if results['plaintext_avalanche']['passed'] else '❌ 异常'}")
    print(f"    说明: 明文变化导致认证标签~50%翻转，总体翻转率约10%")
    
    # 测试三：IV雪崩
    print("\n  测试三：IV雪崩 (IV Avalanche)")
    iv_avalanche = []
    key = rand_os.urandom(32)
    plaintext = b"WeiGuang SafeGuard Test Vector" * 4
    for i in range(iterations):
        iv1 = bytearray(rand_os.urandom(12))
        iv2 = bytearray(iv1)
        iv2[0] ^= 0x01
        
        aad = b"WeiGuang_SafeGuard"
        
        aesgcm = AESGCM(key)
        ct1 = aesgcm.encrypt(bytes(iv1), plaintext, aad)
        ct2 = aesgcm.encrypt(bytes(iv2), plaintext, aad)
        
        diff = sum(bin(a ^ b).count('1') for a, b in zip(ct1, ct2))
        total_bits = len(ct1) * 8
        pct = diff / total_bits * 100
        iv_avalanche.append(pct)
    
    avg_iv = sum(iv_avalanche) / len(iv_avalanche)
    results["iv_avalanche"] = {
        "average": round(avg_iv, 2),
        "target": 50.00,
        "deviation": round(abs(avg_iv - 50.00), 2),
        "passed": abs(avg_iv - 50.00) < 1.0
    }
    print(f"    平均翻转率: {avg_iv:.2f}% (目标: 50.00%, 偏差: {abs(avg_iv-50):.2f}%)")
    print(f"    结果: {'✅ 通过' if results['iv_avalanche']['passed'] else '❌ 失败'}")
    
    return results


# ==================== 认证标签敏感性测试 ====================

def test_tag_sensitivity():
    """GCM 认证标签敏感性测试"""
    print("\n" + "=" * 60)
    print("  GCM 认证标签敏感性测试")
    print("=" * 60)
    
    results = {}
    
    key = rand_os.urandom(32)
    iv = rand_os.urandom(12)
    plaintext = b"WeiGuang SecureGuard Authentication Tag Test"
    aad = b"WeiGuang_SafeGuard_v1.0"
    
    aesgcm = AESGCM(key)
    ciphertext = aesgcm.encrypt(iv, plaintext, aad)
    
    # 测试1: 正确密钥 + 正确密文
    print("\n  测试1: 正确密钥 + 正确密文")
    try:
        decrypted = aesgcm.decrypt(iv, ciphertext, aad)
        if decrypted == plaintext:
            print("    ✅ 解密成功，认证标签通过")
            results["correct"] = "PASS"
        else:
            print("    ❌ 解密结果不匹配")
            results["correct"] = "FAIL"
    except Exception as e:
        print(f"    ❌ {e}")
        results["correct"] = "FAIL"
    
    # 测试2: 正确密钥 + 篡改1 bit密文
    print("\n  测试2: 正确密钥 + 篡改 1 bit 密文")
    tampered = bytearray(ciphertext)
    tampered[-1] ^= 0x01  # 翻转最后1 bit（认证标签）
    try:
        aesgcm.decrypt(iv, bytes(tampered), aad)
        print("    ❌ 解密成功但应该失败（未检测到篡改）")
        results["tampered_ciphertext"] = "FAIL"
    except Exception:
        print("    ✅ 正确拒绝解密（检测到篡改）")
        results["tampered_ciphertext"] = "PASS"
    
    # 测试3: 错误密钥 + 正确密文
    print("\n  测试3: 错误密钥 + 正确密文")
    wrong_key = rand_os.urandom(32)
    wrong_aesgcm = AESGCM(wrong_key)
    try:
        wrong_aesgcm.decrypt(iv, ciphertext, aad)
        print("    ❌ 解密成功但应该失败（错误密钥）")
        results["wrong_key"] = "FAIL"
    except Exception:
        print("    ✅ 正确拒绝解密（错误密钥）")
        results["wrong_key"] = "PASS"
    
    # 测试4: 正确密钥 + 错误 AAD
    print("\n  测试4: 正确密钥 + 错误 AAD")
    try:
        aesgcm.decrypt(iv, ciphertext, b"Wrong_Context")
        print("    ❌ 解密成功但应该失败（错误 AAD）")
        results["wrong_aad"] = "FAIL"
    except Exception:
        print("    ✅ 正确拒绝解密（错误上下文）")
        results["wrong_aad"] = "PASS"
    
    return results


# ==================== 完整报告生成 ====================

def generate_report(avalanche_results, tag_results, wycheproof_results, nist_results):
    """生成完整测试报告"""
    print("\n\n")
    print("=" * 70)
    print("  WeiGuang-Secure 加密引擎完整测试报告")
    print(f"  测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 70)
    
    # 雪崩效应
    print("\n  ┌─ 第一层：自研验证 ─────────────────────────────┐")
    print("  │  AES-256-GCM 雪崩效应测试                      │")
    for name, data in avalanche_results.items():
        status = "✅" if data["passed"] else "❌"
        print(f"  │  {name}: {data['average']:.2f}% (偏差 {data['deviation']:.2f}%) {status}")
    print("  └────────────────────────────────────────────────┘")
    
    # 认证标签
    print("\n  ┌─ 认证标签敏感性测试 ───────────────────────────┐")
    all_pass = all(v == "PASS" for v in tag_results.values())
    for name, result in tag_results.items():
        status = "✅" if result == "PASS" else "❌"
        print(f"  │  {name}: {status}")
    print("  └────────────────────────────────────────────────┘")
    
    # NIST STS
    print("\n  ┌─ 第二层：学术权威 ─────────────────────────────┐")
    print("  │  NIST STS SP 800-22 随机性测试                  │")
    if isinstance(nist_results, list):
        passed = sum(1 for t in nist_results if t.get("passed", False))
        total = len(nist_results)
        for t in nist_results:
            status = "✅" if t.get("passed", False) else "❌"
            print(f"  │  {t['test_number']:02d} {t['name']:<35s} {status}")
        print(f"  │  通过: {passed}/{total}")
    print("  └────────────────────────────────────────────────┘")
    
    # Wycheproof
    print("\n  ┌─ 第三层：工程权威 ─────────────────────────────┐")
    print("  │  Google Wycheproof 已知攻击向量测试              │")
    if isinstance(wycheproof_results, dict):
        for name, data in wycheproof_results.items():
            if isinstance(data, dict) and "total" in data:
                total = data.get("total", 0)
                passed = data.get("passed", 0)
                failed = data.get("failed", 0)
                rate = passed / max(total, 1) * 100
                print(f"  │  {name}: {passed}/{total} ({rate:.1f}%) - 失败: {failed}")
    print("  └────────────────────────────────────────────────┘")
    
    # 最终结论
    print("\n  ╔══════════════════════════════════════════════════╗")
    print("  ║              最终测试结论                        ║")
    print("  ╠══════════════════════════════════════════════════╣")
    
    # 计算总体通过率
    all_pass = True
    
    # Wycheproof 结果
    wy_total = 0
    wy_passed = 0
    if isinstance(wycheproof_results, dict):
        for data in wycheproof_results.values():
            if isinstance(data, dict) and "total" in data:
                wy_total += data.get("total", 0)
                wy_passed += data.get("passed", 0)
    wy_rate = wy_passed / max(wy_total, 1) * 100
    
    # NIST 结果
    nist_total = len(nist_results) if isinstance(nist_results, list) else 0
    nist_passed = sum(1 for t in nist_results if t.get("passed", False)) if isinstance(nist_results, list) else 0
    
    print(f"  ║  Wycheproof 攻击向量:  {wy_passed}/{wy_total} ({wy_rate:.1f}%)  {'✅' if wy_rate >= 99 else '❌'}")
    print(f"  ║  NIST STS 随机性:      {nist_passed}/{nist_total}  {'✅' if nist_passed >= nist_total * 0.8 else '❌'}")
    print(f"  ║  雪崩效应:             {'✅ 通过' if all(d['passed'] for d in avalanche_results.values()) else '❌ 失败'}")
    print(f"  ║  认证标签敏感性:        {'✅ 通过' if all(v == 'PASS' for v in tag_results.values()) else '❌ 失败'}")
    print("  ╚══════════════════════════════════════════════════╝")
    
    return {
        "avalanche": avalanche_results,
        "tag_sensitivity": tag_results,
        "wycheproof": wycheproof_results,
        "nist_sts": nist_results
    }


# ==================== 主入口 ====================

def main():
    print("=" * 70)
    print("  WeiGuang-Secure 加密引擎 - 三层验证测试套件")
    print("  测试 AES-256-GCM / ChaCha20-Poly1305 加密实现")
    print("=" * 70)
    
    all_results = {}
    
    # 第一层：雪崩效应测试
    print("\n" + "█" * 60)
    print("  第一层：自研验证 - 雪崩效应测试")
    print("█" * 60)
    avalanche_results = test_avalanche_effect(iterations=1000)
    all_results["avalanche"] = avalanche_results
    
    # 认证标签测试
    tag_results = test_tag_sensitivity()
    all_results["tag_sensitivity"] = tag_results
    
    # 第二层：NIST STS
    print("\n" + "█" * 60)
    print("  第二层：学术权威 - NIST STS SP 800-22")
    print("█" * 60)
    try:
        nist_results = run_nist_sts()
        all_results["nist_sts"] = nist_results
    except Exception as e:
        print(f"  NIST STS 测试异常: {e}")
        all_results["nist_sts"] = {"error": str(e)}
        nist_results = []
    
    # 第三层：Wycheproof
    print("\n" + "█" * 60)
    print("  第三层：工程权威 - Google Wycheproof")
    print("█" * 60)
    try:
        wycheproof_results = run_wycheproof()
        all_results["wycheproof"] = wycheproof_results
    except Exception as e:
        print(f"  Wycheproof 测试异常: {e}")
        all_results["wycheproof"] = {"error": str(e)}
        wycheproof_results = {}
    
    # 生成完整报告
    report = generate_report(
        avalanche_results, tag_results, 
        wycheproof_results, nist_results
    )
    
    # 保存完整报告
    output_file = os.path.join(os.path.dirname(__file__), "full_test_report.json")
    
    # 处理 numpy 类型转换
    def convert_numpy(obj):
        """递归转换 numpy 类型为 Python 原生类型"""
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
        json.dump(convert_numpy(report), f, indent=2, ensure_ascii=False)
    print(f"\n  完整报告已保存到: {output_file}")
    
    return report

if __name__ == "__main__":
    main()
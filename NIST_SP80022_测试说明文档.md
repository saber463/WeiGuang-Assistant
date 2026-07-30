# NIST SP 800-22 Rev1a 统计随机性测试说明文档

> 文档版本：1.0.0
> 最后更新：2026-06-05
> 关联文件：`fensafe-algorithm/src/main/java/com/weiguangplus/fensafe/NistSp80022Test.java`

---

## 一、概述

### 1.1 测试目的

`NistSp80022Test.java` 实现了美国国家标准与技术研究院（NIST）发布的 Special Publication 800-22 Revision 1a（2010）规范中定义的 9 项核心统计随机性测试。该工具用于验证 FEN-SAFE 加密算法输出的二进制密文数据是否具有足够好的随机性质量。

### 1.2 判定标准

| P-value 范围 | 判定结果 | 含义 |
|-------------|---------|------|
| P-value ≥ 0.05 | **PASS** ✅ | 序列通过测试，无显著统计偏差 |
| P-value < 0.05 | **FAIL** ❌ | 序列存在统计偏差，需排查 |

### 1.3 输入要求

- **输入类型**：原始字节数组（byte[]），密文数据
- **内部转换**：自动将字节转换为二进制位序列（MSB 优先）
- **建议最小数据量**：1MB（约 8×10⁶ bits），推荐使用 10MB 以获得稳定结果
- **适用场景**：加密算法输出验证、PRNG 质量评估、硬件随机源检测

---

## 二、测试列表与算法说明

### 测试 1：Frequency (Monobit) Test — §2.1

**目的**：检验整个序列中 0 和 1 的比例是否接近 1/2。

**算法步骤**：
1. 将位序列转换为 ±1：Xᵢ = 2·bitᵢ - 1
2. 计算累加和：Sₙ = ΣXᵢ
3. 计算检验统计量：s_obs = |Sₙ| / √n
4. P-value = erfc(s_obs / √2)

**前置条件**：n ≥ 100 bits

---

### 测试 2：Block Frequency Test — §2.2

**目的**：检验 M 位块内 1 的比例是否在统计上一致。

**算法步骤**：
1. 将序列分成 N 个不相交的 M 位块（M=128）
2. 对每块计算 πᵢ = 块内 1 的个数 / M
3. 计算卡方统计量：χ² = 4M·Σ(πᵢ - 0.5)²
4. P-value = igamc(N/2, χ²/2)

**默认参数**：M = 128 bits（等于 FEN-SAFE 分组大小）

---

### 测试 3：Runs Test — §2.3

**目的**：检验序列中游程（连续相同比特）的总数是否符合同概率随机性。

**算法步骤**：
1. 计算 π = 序列中 1 的比例
2. 前置条件：|π - 0.5| < 2/√n
3. 计算游程总数 Vₙ = Σr(k) + 1（r(k)=1 当 bit[k]≠bit[k+1]）
4. P-value = erfc(|Vₙ - 2nπ(1-π)| / (2√(2n)·π(1-π)))

**前置条件**：序列必须通过频率测试（π 接近 0.5）

---

### 测试 4：Longest Run of Ones in a Block Test — §2.4

**目的**：检验 M 位块内最长"1"游程的分布是否与随机一致。

**NIST Table 5 参数**：

| n 范围 | M | K | 分桶边界 | π 概率分布 |
|--------|---|---|---------|-----------|
| 128 ≤ n < 6272 | 8 | 4 | ≤1, 2, 3, ≥4 | 0.2148, 0.3672, 0.2305, 0.1875 |
| 6272 ≤ n < 750000 | 128 | 6 | ≤4, 5, 6, 7, 8, ≥9 | 0.1174, 0.2430, 0.2493, 0.1752, 0.1027, 0.1124 |
| n ≥ 750000 | 10000 | 7 | ≤10, 11, 12, 13, 14, 15, ≥16 | 0.0882, 0.2092, 0.2483, 0.1933, 0.1208, 0.0625, 0.0727 |

**算法步骤**：
1. 根据 n 选择 M 和分布参数
2. 分割为 N = n/M 个非重叠块
3. 对每块计算最长连续 1 的游程
4. 按桶边界统计频数
5. 计算 χ² = Σ(oᵢ - eᵢ)²/eᵢ
6. P-value = igamc(K/2, χ²/2)

---

### 测试 5：Binary Matrix Rank Test — §2.5

**目的**：检验序列中不相交子矩阵的秩分布是否与随机一致。

**算法步骤**：
1. 将序列分割成 32×32 的二进制矩阵（每矩阵 1024 bits）
2. 对每个矩阵在 GF(2) 上使用高斯消元法计算秩
3. 统计：满秩（秩=32）、次满秩（秩=31）、其余（秩≤30）
4. 计算卡方检验
5. P-value = igamc(1, χ²/2)

**理论概率**：P(满秩)=0.2888, P(次满秩)=0.5776, P(其余)=0.1336

**前置条件**：至少 38 个矩阵（38912 bits）

---

### 测试 6：Cumulative Sums (Cusum) Test — §2.6

**目的**：检验序列累积和的最大偏移是否与随机游走的预期一致。

**算法步骤**：
1. 将比特转为 ±1：Xᵢ = 2·bitᵢ - 1
2. 计算偏累积和：Sₖ = ΣXᵢ（k=1..n）
3. 定义 z = max|Sₖ|
4. 使用 NIST §2.6 公式计算 P-value（双重无穷级数求和）

**模式**：正向（从头到尾）和反向（从尾到头）分别计算并报告

---

### 测试 7：Approximate Entropy Test (ApEn) — §2.10

**目的**：通过比较 m 位和 m+1 位重叠模式的频率分布，检验序列复杂度。

**算法步骤**：
1. 对 m 和 m+1 分别计算 φ：
   - 构建 n 个重叠 m 位模式（含回绕）
   - 统计每种模式的频率 Cᵢᵐ = countᵢ/n
   - φᵐ = Σ(Cᵢᵐ·ln(Cᵢᵐ))
2. ApEn(m) = φᵐ - φᵐ⁺¹
3. χ² = 2n[ln(2) - ApEn(m)]
4. P-value = igamc(2ᵐ⁻¹, χ²/2)

**默认参数**：m = 2

---

### 测试 8：Serial Test — §2.11

**目的**：检验序列中所有可重叠 m 位模式的频率分布是否均匀。

**算法步骤**：
1. 统计所有重叠 m 位模式的频率（含回绕）
2. 计算 ψ²ₘ = (2ᵐ/n)·Σ(countᵢ²) - n
3. 同样计算 ψ²ₘ₋₁ 和 ψ²ₘ₋₂
4. ∇ψ²ₘ = ψ²ₘ - ψ²ₘ₋₁
5. ∇²ψ²ₘ = ψ²ₘ - 2ψ²ₘ₋₁ + ψ²ₘ₋₂
6. P-value₁ = igamc(2ᵐ⁻², ∇ψ²ₘ/2)
7. P-value₂ = igamc(2ᵐ⁻³, ∇²ψ²ₘ/2)

**默认参数**：m = 16

---

### 测试 9：Discrete Fourier Transform (Spectral) Test — §2.7

**目的**：检验 DFT 频谱中峰值的分布是否与随机白噪声一致。

**算法步骤**：
1. 将比特转为 ±1：Xᵢ = 2·bitᵢ - 1
2. 使用基 2 Cooley-Tukey FFT 计算频谱
3. 计算阈值 T = √(ln(1/0.05)·n)
4. N₀ = 0.95·n/2（期望低于阈值的峰值数）
5. N₁ = 实际低于阈值的峰值数
6. d = (N₁ - N₀) / √(n·0.95·0.05/4)
7. P-value = erfc(|d|/√2)

**性能优化**：取 ≤ n 的最大 2 的幂作为 FFT 长度，上限 MAX_FFT_BITS

---

## 三、架构设计

### 3.1 类结构

```
NistSp80022Test
├── 常量定义 (ALPHA, DEFAULT_BLOCK_SIZE_M, MATRIX_SIZE, MAX_FFT_BITS 等)
├── erfc 近似常数 (Abramowitz & Stegun 7.1.26)
│
├── 公共入口
│   ├── runAllTests(byte[] data) ── 运行全部 9 项测试
│   └── bytesToBits(byte[] data) ── 字节 → 二进制字符串
│
├── 9 项核心测试
│   ├── frequencyTest()
│   ├── blockFrequencyTest()
│   ├── runsTest()
│   ├── longestRunOnesTest()
│   ├── binaryMatrixRankTest()
│   ├── cumulativeSumsTest()
│   ├── approximateEntropyTest()
│   ├── serialTest()
│   └── spectralTest()
│
├── 内部工具函数
│   ├── toBitArray()         ── 字节 → int[] 位数组
│   ├── computeApEnPhi()     ── ApEn φ 值计算
│   ├── computeSerialPsiSq() ── Serial ψ² 统计量
│   ├── computeBinaryRank()  ── GF(2) 矩阵秩 (高斯消元)
│   ├── fft()                ── 基2 Cooley-Tukey FFT
│   │
│   ├── erfc()      ── 互补误差函数 (有理多项式)
│   ├── normalCDF() ── 标准正态 CDF
│   ├── igamc()     ── 正则化上不完全 Gamma 函数
│   │   ├── gammaPSeries()   ── 级数展开 (x < a+1)
│   │   ├── gammaQCFrac()    ── 连分式展开 (x ≥ a+1)
│   │   └── igamcByNormalApprox() ── Wilson-Hilferty 正态近似 (大参数)
│   └── logGamma()  ── ln(Γ(x)) 自然对数 (Stirling-Lanczos)
│
└── main() ── 完整演示入口
```

### 3.2 igamc 实现策略

| 条件 | 方法 | 说明 |
|------|------|------|
| a > 1000 或 x > 1e8 | Wilson-Hilferty 正态近似 | 大参数时避免数值溢出 |
| x < a + 1 | 级数展开 | 精度高，收敛快 |
| x ≥ a + 1 | Lentz 连分式 | 收敛快，避免级数发散 |

### 3.3 数学精度

| 函数 | 方法来源 | 精度 |
|------|---------|------|
| erfc | Abramowitz & Stegun 7.1.26 | < 7.5×10⁻⁸ |
| logGamma | Cephes Stirling-Lanczos | < 2×10⁻¹⁰ |
| igamc (级数) | 修正级数展开 | < 10⁻¹⁴ 相对误差 |
| igamc (连分式) | Modified Lentz | < 10⁻¹⁴ 相对误差 |

---

## 四、使用方式

### 4.1 编译

```bash
cd fensafe-algorithm
javac -encoding UTF-8 -cp target\classes -d target\classes ^
    src\main\java\com\weiguangplus\fensafe\NistSp80022Test.java
```

### 4.2 运行

```bash
java -cp target\classes com.weiguangplus.fensafe.NistSp80022Test
```

### 4.3 编程调用

```java
// 获取加密数据
byte[] ciphertext = ...;

// 运行全部 9 项测试
Map<String, Double> results = NistSp80022Test.runAllTests(ciphertext);

// 逐项检查结果
for (Map.Entry<String, Double> entry : results.entrySet()) {
    String testName = entry.getKey();
    double pValue = entry.getValue();
    boolean passed = pValue >= 0.05;
    System.out.printf("%s: P=%.6f %s%n", testName, pValue, passed ? "PASS" : "FAIL");
}
```

### 4.4 独立测试调用

```java
// 单项测试
int[] bits = NistSp80022Test.toBitArray(data);
double p1 = NistSp80022Test.frequencyTest(bits, bits.length);
double p2 = NistSp80022Test.runsTest(bits, bits.length);
double p3 = NistSp80022Test.blockFrequencyTest(bits, bits.length, 128);
double p4 = NistSp80022Test.cumulativeSumsTest(bits, bits.length, false);
double p5 = NistSp80022Test.spectralTest(bits, bits.length);
```

---

## 五、输出格式说明

### 5.1 控制台表格

```
┌──────────────────────────────────────────────┬────────────┬────────┐
│ 测试名称                                     │ P-value    │ 结果   │
├──────────────────────────────────────────────┼────────────┼────────┤
│ Frequency (Monobit)                          │   0.596584 │ PASS   │
│ Block Frequency (M=128)                      │   0.209735 │ PASS   │
│ ...                                         │            │        │
└──────────────────────────────────────────────┴────────────┴────────┘
```

### 5.2 统计汇总

| 指标 | 含义 |
|------|------|
| PASS 率 | 通过项数 / 总项数（正常 ≥ 90%） |
| FAIL 项 | 需重点排查的测试项 |
| ERROR 项 | 因数据不足无法执行的测试项 |
| 测试耗时 | 包括数据生成和全部测试执行时间 |

---

## 六、已知问题与限制

1. **DFT Test 性能**：对大输入（>10MB）进行 FFT 时，FFT 长度上限为 2²⁰ = 1,048,576 点，超长序列会被截断。
2. **Serial Test m=16 内存**：需要 2¹⁶ = 65536 个计数器的数组（约 256KB），对极大数据量可能不够精确。
3. **二进制矩阵秩测试**：10MB 数据约生成 81,920 个 32×32 矩阵，每个矩阵需高斯消元，总计算量约 2.6 亿次 GF(2) 运算。
4. **ApEn 测试**：m=2 时对 80M 位数据需约 4.15 亿次迭代，是耗时最长的测试之一。

---

## 七、变更历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2026-06-05 | 首次发布，实现全部 9 项 NIST SP800-22 Rev1a 测试 | 微光畅行项目组 |

---

## 八、参考文献

1. NIST Special Publication 800-22 Revision 1a, "A Statistical Test Suite for Random and Pseudorandom Number Generators for Cryptographic Applications", April 2010.
2. Abramowitz, M. and Stegun, I. A., "Handbook of Mathematical Functions", Dover, 1964.
3. Press, W. H. et al., "Numerical Recipes in C", 2nd ed., Cambridge University Press, 1992.
4. Schilling, M. F., "The Longest Run of Heads", The College Mathematics Journal, Vol. 21, No. 3, 1990.
5. Knuth, D. E., "The Art of Computer Programming, Vol. 2: Seminumerical Algorithms", 3rd ed., Addison-Wesley, 1998.
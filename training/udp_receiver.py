"""
手势数据 UDP 接收器
=====================
功能：监听 Android 手机通过 UDP 发送的手势关键点数据，实时保存为 CSV 训练文件

架构：
    手机端（Android）                      PC端（本脚本）
    ┌─────────────────┐                   ┌──────────────────┐
    │ 摄像头 → MediaPipe│   UDP (JSON)     │ 监听端口 9999     │
    │ → 21个关键点坐标  │ ──────────────→  │ → 解析 JSON       │
    │ → 打标签 → 发送   │   WiFi局域网     │ → 追加到 CSV      │
    └─────────────────┘                   │ → 实时统计显示    │
                                          └──────────────────┘

使用方式：
    python udp_receiver.py

    可选参数：
    --port 9999        UDP监听端口（默认9999）
    --output dataset   输出目录（默认dataset）
    --label 0          手动设置标签编号（0-9），不指定则使用手机发送的标签

操作说明：
    - 手机和PC连接同一WiFi
    - 在手机APP中开启"UDP数据采集"开关，输入PC的IP地址
    - 本脚本自动接收并保存数据
    - 按 Ctrl+C 停止接收

协议格式（JSON）：
    {
        "label": 0,              // 手势标签编号（0-9）
        "label_name": "fist",    // 手势名称
        "landmarks": [           // 21个关键点 × 3坐标 = 63个float
            x0, y0, z0, x1, y1, z1, ..., x20, y20, z20
        ],
        "timestamp": 1234567890, // 时间戳
        "confidence": 0.95       // 识别置信度（可选）
    }
"""

import argparse
import csv
import json
import os
import signal
import socket
import sys
from datetime import datetime
from collections import defaultdict

# ─────────────────────────────────────────────────────────────────────────────
# 配置
# ─────────────────────────────────────────────────────────────────────────────

DEFAULT_PORT = 9999
BUFFER_SIZE = 4096
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "dataset")

LABEL_NAMES = [
    "fist", "open_palm", "thumbs_up", "point_index", "peace",
    "ok_sign", "wave", "heart", "call_me", "neutral"
]

LABEL_NAMES_ZH = [
    "握拳(SOS)", "手掌张开(停止)", "竖大拇指(确认)", "食指指向(方向)",
    "剪刀手(胜利)", "OK手势", "摆手(问候)", "比心(谢谢)",
    "打电话", "无手势"
]


class UDPGestureReceiver:
    """
    UDP手势数据接收器

    功能：
    - 监听UDP端口，接收来自Android手机的JSON数据包
    - 实时解析21个手部关键点坐标
    - 追加写入CSV文件
    - 显示实时统计（每个手势已采集帧数、总帧数、最新手势）
    """

    def __init__(self, port: int = DEFAULT_PORT, output_dir: str = OUTPUT_DIR,
                 force_label: int = None):
        """
        参数：
            port: UDP监听端口
            output_dir: CSV输出目录
            force_label: 强制标签（覆盖手机发送的标签），None表示使用手机标签
        """
        self.port = port
        self.output_dir = output_dir
        self.force_label = force_label
        self.running = True

        # 统计信息
        self.total_received = 0
        self.total_saved = 0
        self.total_errors = 0
        self.label_counts = defaultdict(int)
        self.last_gesture = None
        self.last_confidence = 0.0

        # CSV文件路径
        os.makedirs(self.output_dir, exist_ok=True)
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.csv_path = os.path.join(self.output_dir, f"phone_gesture_data_{timestamp}.csv")
        self.csv_file = None
        self.csv_writer = None

        # 初始化CSV文件
        self._init_csv()

        # 创建UDP socket
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.settimeout(1.0)  # 1秒超时，允许检查running标志

        # 注册信号处理（Ctrl+C）
        signal.signal(signal.SIGINT, self._signal_handler)

    def _signal_handler(self, sig, frame):
        """Ctrl+C 信号处理"""
        print("\n\n[中断] 正在停止接收...")
        self.running = False

    def _init_csv(self):
        """初始化CSV文件，写入表头"""
        self.csv_file = open(self.csv_path, "w", newline="", encoding="utf-8")
        self.csv_writer = csv.writer(self.csv_file)

        # 表头：x0,y0,z0, x1,y1,z1, ..., x20,y20,z20, label
        columns = []
        for i in range(21):
            columns.extend([f"x{i}", f"y{i}", f"z{i}"])
        columns.append("label")
        self.csv_writer.writerow(columns)
        self.csv_file.flush()

    def _save_to_csv(self, landmarks: list, label: int):
        """将单帧数据追加到CSV文件"""
        row = landmarks + [label]
        self.csv_writer.writerow(row)
        self.csv_file.flush()
        self.total_saved += 1
        self.label_counts[label] += 1

    def _print_status(self):
        """打印实时统计信息到控制台"""
        # 清屏
        os.system("cls" if os.name == "nt" else "clear")

        print("=" * 60)
        print("  手势数据 UDP 接收器 —— 微光同行APP")
        print("=" * 60)
        print(f"\n  监听端口: {self.port}")
        print(f"  输出文件: {self.csv_path}")
        if self.force_label is not None:
            print(f"  强制标签: [{self.force_label}] {LABEL_NAMES[self.force_label]}")
        else:
            print(f"  标签模式: 跟随手机发送的标签")
        print(f"\n  PC IP地址: {self._get_local_ip()}")
        print(f"  请在手机APP中输入上述IP地址")

        print(f"\n  ── 接收统计 ──")
        print(f"  总接收: {self.total_received}  |  已保存: {self.total_saved}  |  错误: {self.total_errors}")

        if self.last_gesture:
            conf_str = f"置信度: {self.last_confidence:.0%}" if self.last_confidence > 0 else ""
            print(f"  最新手势: {self.last_gesture}  {conf_str}")

        print(f"\n  ── 各手势采集进度 ──")
        for idx, (name, zh_name) in enumerate(zip(LABEL_NAMES, LABEL_NAMES_ZH)):
            count = self.label_counts[idx]
            bar_len = 25
            filled = min(bar_len, count // 10)
            bar = "█" * filled + "░" * (bar_len - filled)
            marker = " ← 最新" if idx == self.last_gesture_idx() else ""
            print(f"  [{idx}] {name:12s} {zh_name:12s} | {bar} | {count:4d} 帧{marker}")

        print(f"\n  ── 操作提示 ──")
        print(f"  按 Ctrl+C 停止接收")
        print(f"  按 R 键重新开始（清空数据）")
        print(f"  按 L 键切换标签模式（0-9手动 / 自动）")

    def _get_local_ip(self) -> str:
        """获取本机局域网IP地址"""
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
            s.close()
            return ip
        except Exception:
            return "无法获取（请手动查看）"

    def last_gesture_idx(self) -> int:
        """获取最新手势的标签编号"""
        if self.last_gesture is None:
            return -1
        try:
            return LABEL_NAMES.index(self.last_gesture)
        except ValueError:
            return -1

    def start(self):
        """启动UDP监听主循环"""
        try:
            self.sock.bind(("0.0.0.0", self.port))
        except OSError as e:
            print(f"[错误] 无法绑定端口 {self.port}: {e}")
            print("提示: 端口可能被占用，尝试其他端口: python udp_receiver.py --port 9998")
            sys.exit(1)

        print(f"\n[启动] UDP接收器已启动，监听端口 {self.port}")
        print(f"[提示] 本机IP: {self._get_local_ip()}")
        print(f"[提示] 等待手机连接...\n")

        self._print_status()

        while self.running:
            try:
                # 接收数据
                data, addr = self.sock.recvfrom(BUFFER_SIZE)
                self.total_received += 1

                # 解析JSON
                try:
                    packet = json.loads(data.decode("utf-8"))
                except json.JSONDecodeError:
                    self.total_errors += 1
                    continue

                # 提取数据
                landmarks = packet.get("landmarks", [])
                label = packet.get("label", -1)
                label_name = packet.get("label_name", "unknown")
                confidence = packet.get("confidence", 0.0)

                # 验证数据完整性
                if len(landmarks) != 63:
                    print(f"[警告] 关键点数量异常: {len(landmarks)} (期望63)，来自 {addr[0]}")
                    self.total_errors += 1
                    continue

                if label < 0 or label >= len(LABEL_NAMES):
                    if self.force_label is None:
                        print(f"[警告] 无效标签: {label}，来自 {addr[0]}")
                        self.total_errors += 1
                        continue

                # 使用强制标签（如果设置了）
                final_label = self.force_label if self.force_label is not None else label

                # 保存到CSV
                self._save_to_csv(landmarks, final_label)

                # 更新状态
                self.last_gesture = label_name if self.force_label is None else LABEL_NAMES[self.force_label]
                self.last_confidence = confidence

                # 刷新显示（每10帧刷新一次，减少控制台闪烁）
                if self.total_saved % 10 == 0:
                    self._print_status()

            except socket.timeout:
                # 超时，继续循环检查running标志
                continue
            except Exception as e:
                print(f"[错误] {e}")
                self.total_errors += 1

        self.cleanup()

    def cleanup(self):
        """清理资源"""
        self.running = False
        if self.csv_file:
            self.csv_file.close()
        if self.sock:
            self.sock.close()

        print(f"\n[停止] 接收器已停止")
        print(f"  总接收: {self.total_received} 包")
        print(f"  已保存: {self.total_saved} 帧")
        print(f"  错误数: {self.total_errors}")
        print(f"  数据文件: {self.csv_path}")

        if self.total_saved > 0:
            print(f"\n  下一步: 用这些数据训练模型")
            print(f"  python train_gesture_deep.py --csv {self.csv_path}")
            print(f"  或混合合成数据一起训练:")
            print(f"  python train_gesture_deep.py --csv dataset/realistic_gesture_data.csv --csv {self.csv_path}")


def main():
    parser = argparse.ArgumentParser(
        description="手势数据UDP接收器 —— 接收手机发送的手势关键点数据",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  python udp_receiver.py                          # 默认端口9999，使用手机标签
  python udp_receiver.py --port 8888              # 自定义端口
  python udp_receiver.py --label 0                # 强制标记为握拳(0)
  python udp_receiver.py --label 1 --port 9998    # 自定义端口+标签
        """
    )
    parser.add_argument("--port", type=int, default=DEFAULT_PORT,
                        help=f"UDP监听端口（默认: {DEFAULT_PORT}）")
    parser.add_argument("--output", type=str, default=OUTPUT_DIR,
                        help=f"CSV输出目录（默认: {OUTPUT_DIR}）")
    parser.add_argument("--label", type=int, default=None,
                        help="强制标签编号0-9（不指定则使用手机发送的标签）")
    args = parser.parse_args()

    # 验证标签
    if args.label is not None and (args.label < 0 or args.label >= len(LABEL_NAMES)):
        print(f"[错误] 无效标签编号: {args.label}，有效范围: 0-{len(LABEL_NAMES)-1}")
        print("标签对照:")
        for idx, (name, zh) in enumerate(zip(LABEL_NAMES, LABEL_NAMES_ZH)):
            print(f"  {idx} = {name} ({zh})")
        sys.exit(1)

    receiver = UDPGestureReceiver(
        port=args.port,
        output_dir=args.output,
        force_label=args.label
    )
    receiver.start()


if __name__ == "__main__":
    main()
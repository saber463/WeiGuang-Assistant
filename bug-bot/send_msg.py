"""
向飞书用户发送测试消息
通过SSH连接服务器，使用lark-cli查找用户并发送消息
"""
import paramiko
import json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('47.108.149.191', 22, 'root', 'WgTx2026!@Alibaba', timeout=30)

# 步骤1: 获取用户列表
print("=== 步骤1: 获取用户列表 ===")
cmd = 'lark-cli api GET /open-apis/contact/v3/users --params \'{"page_size":20}\' --as bot 2>&1'
stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
out = stdout.read().decode()
err = stderr.read().decode()

# 尝试解析JSON
try:
    data = json.loads(out)
except json.JSONDecodeError:
    # 可能输出中包含非JSON内容
    print(f"原始输出: {out[:1000]}")
    if err:
        print(f"stderr: {err[:500]}")
    ssh.close()
    exit()

if err:
    print(f"stderr: {err[:500]}")

if data.get("ok") and data.get("data", {}).get("items"):
    users = data["data"]["items"]
    print(f"找到 {len(users)} 个用户:")
    for u in users:
        print(f"  用户: {u.get('name')} | open_id: {u.get('open_id')} | email: {u.get('email')}")

    # 步骤2: 向第一个用户发送测试消息
    first_user = users[0]
    user_id = first_user["open_id"]
    user_name = first_user.get("name", "未知")

    print(f"\n=== 步骤2: 向 {user_name} ({user_id}) 发送测试消息 ===")

    card = {
        "schema": "2.0",
        "config": {"width_mode": "compact"},
        "body": {
            "direction": "vertical",
            "elements": [
                {
                    "tag": "markdown",
                    "content": "**🔧 BUG诊断机器人 测试消息**\n\n机器人已上线运行中！\n\n**如何使用：**\n在飞书里直接回复这条消息，输入报错信息（如「点击按钮闪退」），机器人会自动搜索匹配的BUG记录并回复诊断结果。\n\n也可以发送「**帮助**」查看使用说明。"
                }
            ]
        }
    }
    card_json = json.dumps(card, ensure_ascii=False)

    send_cmd = f"lark-cli im +messages-send --user-id {user_id} --msg-type interactive --content '{card_json}' --as bot 2>&1"
    stdin, stdout, stderr = ssh.exec_command(send_cmd, timeout=30)
    send_out = stdout.read().decode()
    send_err = stderr.read().decode()

    print(f"发送结果: {send_out[:500]}")
    if send_err:
        print(f"发送stderr: {send_err[:500]}")

    print(f"\n=== 完成！请 {user_name} 在飞书中查看并回复此消息来测试 ===")
else:
    print(f"获取用户列表失败: {out[:500]}")

ssh.close()
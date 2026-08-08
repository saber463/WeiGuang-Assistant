"""
WSGI入口文件 - 用于gunicorn启动
功能：加载Flask应用，供生产环境gunicorn使用
"""
from app import app

if __name__ == "__main__":
    app.run()
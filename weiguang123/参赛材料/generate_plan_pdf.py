# -*- coding: utf-8 -*-
"""
将项目计划书 Markdown 转换为 PDF
使用 Python markdown 库 + Playwright 导出
"""

import sys
from pathlib import Path
import markdown

BASE_DIR = Path(__file__).parent
MD_FILE = BASE_DIR / "微光科技_微光同行_项目计划书.md"
OUTPUT_PDF = BASE_DIR / "微光科技_微光同行_项目计划书.pdf"

def build_html():
    """读取Markdown并转换为带样式的HTML"""
    with open(MD_FILE, 'r', encoding='utf-8') as f:
        md_content = f.read()
    
    # 使用Python-Markdown转换，扩展：表格、代码高亮、目录
    md = markdown.Markdown(extensions=['tables', 'fenced_code', 'codehilite', 'toc'])
    body_html = md.convert(md_content)
    
    html = f'''<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<style>
  @page {{
    size: A4;
    margin: 2cm 1.8cm;
    @top-center {{
      content: "微光科技 · 微光同行 项目计划书";
      font-size: 9px;
      color: #999;
      font-family: "Microsoft YaHei", sans-serif;
    }}
    @bottom-center {{
      content: "第 " counter(page) " 页";
      font-size: 9px;
      color: #999;
      font-family: "Microsoft YaHei", sans-serif;
    }}
  }}
  body {{
    font-family: "Microsoft YaHei", "PingFang SC", "SimHei", sans-serif;
    color: #333;
    line-height: 1.8;
    font-size: 12px;
  }}
  h1 {{
    font-size: 24px;
    color: #E65100;
    text-align: center;
    margin-bottom: 4px;
    border-bottom: 2px solid #FF9800;
    padding-bottom: 10px;
  }}
  h2 {{
    font-size: 18px;
    color: #E65100;
    margin-top: 28px;
    margin-bottom: 12px;
    border-left: 4px solid #FF9800;
    padding-left: 10px;
  }}
  h3 {{
    font-size: 15px;
    color: #FF6F00;
    margin-top: 22px;
    margin-bottom: 8px;
  }}
  h4 {{
    font-size: 13px;
    color: #FF9800;
    margin-top: 16px;
    margin-bottom: 6px;
  }}
  p {{
    margin: 8px 0;
    text-align: justify;
  }}
  table {{
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 11px;
    page-break-inside: avoid;
  }}
  table th {{
    background: #FF9800;
    color: white;
    padding: 8px 10px;
    text-align: left;
    font-weight: 700;
  }}
  table td {{
    padding: 8px 10px;
    border-bottom: 1px solid #eee;
  }}
  table tr:nth-child(even) td {{
    background: #FFF8E1;
  }}
  code {{
    background: #FFF3E0;
    color: #E65100;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 11px;
    font-family: "Consolas", "Courier New", monospace;
  }}
  pre {{
    background: #1a1a2e;
    color: #FFB74D;
    padding: 14px 18px;
    border-radius: 8px;
    font-size: 10px;
    overflow-x: auto;
    line-height: 1.5;
    page-break-inside: avoid;
  }}
  pre code {{
    background: none;
    color: #FFB74D;
    padding: 0;
  }}
  blockquote {{
    background: #FFF3E0;
    border-left: 4px solid #FF9800;
    margin: 12px 0;
    padding: 10px 16px;
    border-radius: 4px;
    color: #E65100;
    font-size: 12px;
  }}
  ul, ol {{
    margin: 8px 0;
    padding-left: 24px;
  }}
  li {{
    margin: 4px 0;
  }}
  strong {{
    color: #E65100;
  }}
  hr {{
    border: none;
    border-top: 1px solid #eee;
    margin: 20px 0;
  }}
  .cover {{
    text-align: center;
    padding: 60px 0 30px;
    page-break-after: always;
  }}
  .cover h1 {{
    font-size: 30px;
    border: none;
    margin-bottom: 16px;
  }}
  .cover .cover-sub {{
    font-size: 16px;
    color: #FF6F00;
    margin-bottom: 10px;
  }}
  .cover .cover-meta {{
    font-size: 13px;
    color: #666;
    margin: 6px 0;
  }}
  .cover .divider {{
    width: 60px;
    height: 3px;
    background: #FF9800;
    margin: 24px auto;
  }}
  .footer-note {{
    font-size: 10px;
    color: #999;
    text-align: center;
    margin-top: 40px;
    border-top: 1px solid #eee;
    padding-top: 16px;
  }}
</style>
</head>
<body>
<div class="cover">
  <h1>微光同行 —— 智能助残服务平台</h1>
  <div class="cover-sub">2026 AI助残创新创意大赛 · 创意赛道 项目计划书</div>
  <div class="divider"></div>
  <p class="cover-meta">申报单位/团队：微光科技（WeiGuang Tech）</p>
  <p class="cover-meta">所属单位：天府新区通用航空职业学院</p>
  <p class="cover-meta">项目口号：以AI之手，搭建听障人士与世界沟通的桥梁</p>
  <p class="cover-meta">编制日期：2026年7月28日</p>
</div>
{body_html}
<div class="footer-note">
  <p>本计划书为2026 AI助残创新创意大赛 · 创意赛道参赛材料</p>
  <p>团队：微光科技（WeiGuang Tech）| 所属单位：天府新区通用航空职业学院</p>
</div>
</body>
</html>'''
    return html


def main():
    from playwright.sync_api import sync_playwright
    
    print("=" * 60)
    print("  微光科技 · 项目计划书 Markdown → PDF 转换器")
    print("=" * 60)
    
    if not MD_FILE.exists():
        print(f"[ERROR] 计划书文件不存在: {MD_FILE}")
        sys.exit(1)
    
    print(f"\n[步骤1] 读取Markdown: {MD_FILE.name}")
    html_content = build_html()
    print(f"[OK] HTML已生成 ({len(html_content)} 字符)")
    
    # 写入临时HTML
    temp_html = BASE_DIR / "_temp_plan.html"
    temp_html.write_text(html_content, encoding='utf-8')
    
    print(f"\n[步骤2] 生成PDF...")
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        page.goto(temp_html.resolve().as_uri(), wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.pdf(path=str(OUTPUT_PDF), format="A4", margin={"top": "2cm", "bottom": "2cm", "left": "1.8cm", "right": "1.8cm"})
        browser.close()
    
    temp_html.unlink(missing_ok=True)
    
    print(f"\n[SUCCESS] PDF生成完成！")
    print(f"  输出路径: {OUTPUT_PDF}")
    print(f"  文件大小: {OUTPUT_PDF.stat().st_size / 1024:.1f} KB")


if __name__ == '__main__':
    main()
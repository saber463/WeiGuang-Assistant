path = r"F:\java\weiguangplus\app\build.gradle.kts"
with open(path, "r", encoding="utf-8-sig") as f:
    content = f.read()

# Fix: replace literal backtick-r backtick-n with actual line break
content = content.replace("`r`n", "\n")
content = content.replace("implementation(`"androidx.lifecycle:lifecycle-runtime-ktx:2.6.1`")`n    implementation(`"androidx.lifecycle:lifecycle-runtime-compose:2.6.1`")", 'implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")\n    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")')

# Remove duplicate line if exists
lines = content.split("\n")
unique_lines = []
seen = set()
for line in lines:
    stripped = line.strip()
    if stripped and stripped not in seen:
        seen.add(stripped)
        unique_lines.append(line)
    elif not stripped:
        unique_lines.append(line)

content = "\n".join(unique_lines)
with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("build.gradle.kts fixed")

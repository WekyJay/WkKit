#!/bin/bash

# 批量替换异常处理工具脚本
# 将所有的 e.printStackTrace() 替换为统一的异常处理

echo "开始替换异常处理..."

# 1. 首先在所有Java文件中添加ExceptionHandler导入
find src/main/java -name "*.java" -type f | while read file; do
    # 检查是否已经导入了ExceptionHandler
    if grep -q "import cn.wekyjay.www.wkkit.util.ExceptionHandler;" "$file"; then
        echo "跳过: $file (已导入ExceptionHandler)"
    elif grep -q "printStackTrace" "$file"; then
        echo "处理: $file"
        # 在import语句后添加ExceptionHandler导入
        sed -i '/import cn.wekyjay.www.wkkit\..*;/a import cn.wekyjay.www.wkkit.util.ExceptionHandler;' "$file"
    fi
done

echo "导入语句添加完成"

# 2. 替换异常处理
# 通用异常处理替换
sed -i 's/\([[:alnum:]]*\)\.printStackTrace()/ExceptionHandler.handle("未知操作", \1)/g' $(find src/main/java -name "*.java" -type f)

# 特定类型的异常处理
# IO异常
sed -i 's/} catch (IOException \(e\|ex\|exception\)) {/} catch (IOException \1) {/g' $(find src/main/java -name "*.java" -type f)
sed -i 's/} catch (java.io.IOException \(e\|ex\|exception\)) {/} catch (IOException \1) {/g' $(find src/main/java -name "*.java" -type f)

# SQL异常
sed -i 's/} catch (SQLException \(e\|ex\|exception\)) {/} catch (SQLException \1) {/g' $(find src/main/java -name "*.java" -type f)
sed -i 's/} catch (java.sql.SQLException \(e\|ex\|exception\)) {/} catch (SQLException \1) {/g' $(find src/main/java -name "*.java" -type f)

echo "异常处理替换完成"

# 3. 统计替换结果
echo "替换统计:"
echo "总共找到的 printStackTrace: $(grep -r "printStackTrace" src/main/java --include="*.java" | wc -l)"
echo "替换后的 ExceptionHandler.handle: $(grep -r "ExceptionHandler.handle" src/main/java --include="*.java" | wc -l)"

echo "完成!"
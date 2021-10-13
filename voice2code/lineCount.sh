echo "File num:"
find src -name "*.java" | wc -l
echo "Line num:"
find src -name "*.java" | xargs cat | wc -l

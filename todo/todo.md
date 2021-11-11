[√] 数组下标索引：`ns[i]`，only support simple case, not `getNames()[15*15]`
[√] 表达式括号：`3 * (4 + 5)`
[√] else without condition
[√] switch's default case.
[√] interface 中的函数只需要声明，不需要定义，需要单独支持...
[√] 字符串句子: `string hello world`
[√] 异常处理
[√] 泛型
[√] move next 不够智能
[√] let1, let2 not correct, return 1, return2 也需要检查
[ ] 对象形式的初始化：`int[] ns = { 1, 4, 9, 16, 25}`
[√] 一套测试和验证规范，并整理一个测试集
[√] `switch` 用 `move next` 无法跳出整个`SwitchStmt`, 见 `14Switch9.vocieJava`
[] 更多的测试来完善代码逻辑
[] 从文件恢复状态的机制：如果只有代码以及 HoleAST 的数据，如何恢复出来
[] `class pattern` 中，`ClassModifier`可选，且可以多个
[] `method pattern`中,`throws Exception`还不支持
[] `type pattern`中,`extends`未实现
[] 参数定义中，如何正确识别类型，比如 `type string variable name` 会被识别为: `string name`; `type String variable name`被识别为`String name`。我们期望的为第二种
[] `variable names index i`中,`variable`可选
[] `null`: https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/expr/NullLiteralExpr.html
[] `byte, float, short`没有?

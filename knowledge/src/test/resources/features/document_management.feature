Feature: 文档管理
  作为一个知识库用户
  我希望能够管理文档
  以便组织和检索知识内容

  Background:
    Given 系统已经启动
    And 数据库已经初始化

  Scenario: 扫描新文档
    Given 存在一个包含文档的目录 "/test/documents"
    And 目录中包含以下文件:
      | 文件名        | 类型 | 大小  |
      | readme.md    | md   | 1024  |
      | guide.txt    | txt  | 512   |
      | manual.pdf   | pdf  | 2048  |
    When 我执行文档扫描命令，扫描路径为 "/test/documents"
    And 文件类型过滤器为 ["md", "txt"]
    Then 应该发现 2 个文档
    And 应该创建 2 个新文档记录
    And 所有新文档的状态应该为 "PENDING"

  Scenario: 解析Markdown文档
    Given 存在一个状态为 "PENDING" 的Markdown文档
    And 文档路径为 "/test/sample.md"
    And 文档内容为:
      """
      # 示例文档
      
      这是一个**示例**文档，包含一些*格式化*文本。
      
      ## 章节1
      
      这里是章节1的内容。
      """
    When 我执行文档解析命令
    Then 文档状态应该变为 "COMPLETED"
    And 应该提取出文档元数据:
      | 字段     | 值        |
      | 标题     | 示例文档   |
      | 语言     | zh        |
      | 字数     | 大于0     |
    And 应该生成清理后的文本内容
    And 应该创建内容分段

  Scenario: 处理不存在的文档
    Given 不存在ID为 "non-existent-doc" 的文档
    When 我尝试解析ID为 "non-existent-doc" 的文档
    Then 应该抛出 "文档不存在" 异常

  Scenario: 删除文档
    Given 存在一个ID为 "test-doc-1" 的文档
    When 我执行删除文档命令，文档ID为 "test-doc-1"
    Then 文档应该被从系统中删除
    And 查询该文档应该返回空结果
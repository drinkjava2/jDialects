版本发布记录  
1.0.0 第一版发布，支持Java7
1.0.1 更正一些Bug  
1.0.2 完善DDL功能  
1.0.3 更正一些Bug，压缩了一下尺寸; 降级到Java6，以支持更多开发环境。  
1.0.4 修正DDL中Unique约束不支持多个列的Bug
1.0.5 添加JPA等Annotation支持，原来的index、unique、fkey()等方法设计的太复杂，这次作了较大改动，采用singleXxx()方法代替。为了与JPA命名不冲突，Table和Column分别改名为TableModel和ColumnModel。  
1.0.6 配合jSqlBox第一版发布，做了较多改进。TableMode添加了entityClass、alias字段, ColumnModel类方法名作如下改动: pojoField改为entityField, pojoField改为entityField, autoID改为autoId, identity改为identityId,tableGenerator和sequence改为idGenerator。  

Release History
1.0.0 First Version
1.0.1 Fix some bugs
1.0.2 Improve DDL
1.0.3 Fix some bugs, shrink size from 200k to 165k, downgrade to support Java6
1.0.4 Fix unique constraint doesn't support multiple columns bug
1.0.5 Add JPA annotation support, add singleXxxx() methods
1.0.6 Lots improvements, for jSqlBox. In ColumnModel made below method name change: pojoField->entityField, pojoField->entityField, autoID->autoId, identity->identityId,tableGenerator&sequence->idGenerator。


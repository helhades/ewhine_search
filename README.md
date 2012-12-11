ewhine_search
=============

The ewhine search system use zoie.

#安装
git clone git@github.com:jimrok/ewhine_search.git

cd ewhine_search

java -jar server.jar
如果在Mac的控制台下，使用
java -Dfile.encoding=UTF-8 -jar server.jar

#索引
在ewhine_NB的目录下，执行
rake index:build

#搜索
在控制台上输入http://localhost:8888/search
可以看到一个搜索页面，输入用户id，例如1，输入关键词，例如：card
点“搜索”，可以看到结果。
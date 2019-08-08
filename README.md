# 九尾狐jenkins构建插件

## 简介
九尾狐jenkins构建插件，九尾狐发起构建调用插件api，插件启动对应job的构建，构建完成后调用九尾狐构建回调接口，返回构建信息


## 环境依赖
* jenkins
 
## 部署步骤
- 1、运行mvn clean package -DskipTests进行打包
- 2、打包完成后将.hpi包上传到jenkins
- 3、重启jenkins使插件生效


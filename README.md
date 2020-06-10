# my-rpc
使用springboot，netty，zookeeper写的一个简单RPC框架。该项目分成三个模块，各个模块包含的内容如下：  
模块一、rpc-api：该模块定义了rpc-client和rpc-server之间请求和相应等数据格式以及异常处理。  
模块二、rpc-client：  
  1.定义了@RemoteInvoke注解  
  2.使用springboot提供的基于cglib的增强器Enhancer进行动态代理  
  3.容器动态后会到zookeeper获取服务提供方地址信息，并监控变化情况  
模块三、rpc-server:  
  1.定义了@Remote注解  
  2.对使用了@Remote注解的类进行统一管理  
  3.向zookeeper写入服务器地址信息  

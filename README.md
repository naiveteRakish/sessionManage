# sessionManage
该项目提供了基于redis的分布式会话管理 和 web应用统一接口的验签 还有xss过滤和crsf令牌
# Primus 博智互联网电商平台

----

### 关于项目

本工程基于JDK1.8进行开发,使用方只需要打一个jar 进行依赖，做一个简单的配置即可使用。有问题可以直接找qq614320133 我可以给你一个成功的demo工程
本工程使用postman测试通过: 1.表单参数提交 2.表单文件提交  3.appliction/json提交 4.文件上传二进制提交未做测试 ...
本工程使用postman测试通过: 1.xss过滤基于aop，controller接口使用JSP(jsp则无法做xss过滤参数)，controller接口返回到body(xss过滤支持返回List、Map、数组、Object、四种类型的相互嵌套以及Object父类也能过滤到)
CSRF令牌测试已通过
会话管理测试已通过

### 关于签名以及验签方式 (如果设置了 SessionUtil.SOURCE_FLAG 静态属性，则手机端签名必须遵守该静态属性上的说明 并 签名后需要先按以下规则排序再转换为大写， 然后根据规则 : [A-->Z	B-->C	T-->I	P-->Q	U-->R	M-->E] 进行替换，然后进行签名 )
这里我并没有和手机端进行测试，需要后续使用的时候测试，手机端校验方式是否正确。
`签名`:我们http传输参数，参数体现通常两种形态
	1.键值对的体现		2.数据存入body中(无键只有值)
```
只存在键值对，则按照键进行字典排序，然后按照排序结果进行字符串拼接（键=值，中间使用'&'分割）
例1: param1=value1&param2=value2
只存在body数据，无键只有值 ，则无需拼接，直接进行签名。
例2:{"name":"xiaoming","age":18,"id":"UUID123"}
存在键值对以及body数据 拼接方式和例1类似，只不过在最后拼接上body体数据
例3:param1=value1&param2=value2&{"name":"xiaoming","age":18,"id":"UUID123"}
表单提交具有参数对和文件
 <form><input type="text" name="param1" value ="value1" ><input type="text" name="param2" value ="value2" ><input type="file" name="myfile" value ="xxx" ></form>
现将param1 、param2、myfile 进行字典排序 然后就想拼接
例4:param1=value1&param2=value2&myfile=`原文件名`
```
本工程秘钥生成算法采用 	`rsa` ，签名算法采用 `SHA1withRSA` . 可以在`com.biz.primus.base.session.util.SessionUtil` 中查看并更改使用算法。
该类中可配置的属性，都未使用fianl修饰，为了方便更改，尽量将可更改属性移动到了SessionUtil类中。
该类中必须更改的属性有:
```
出现异常的返回code码(目前是乱写的码):
/**
	 * TODO 用户未认证code ???
	 */
	public static int UNVERIFIED = 3332333;
	/**
	 * TODO 验签失败code ???
	 */
	public static int VERIFY_FAIL = 1111111;
	/**
	 * TODO 无效的CSRF令牌
	 */
	public static int INVALID_CSRF = 2222222;
	/**
	 * 缺失必要字段
	 */
	public static int MISSING_FIELD = 444444444;
	/**
	 * 不支持的操作
	 */
	public static int NOT_SUPPORT_OPERATE = 555555;

	/**
	 * `返回的modle包路径 如果使用xss返回值过滤，必须填写`(这个如果配置了xss过滤，则必须指定你返回到前端对象所在的基础包路径，底层过滤是直接判断类型的基础包来决定是否进行xss过滤)
	 */
	public static String PACKAGE_NAME ;
```
签名所需要的私钥，会在用户登录成功之后在header头中得到返回。例: PRIVATE_KEY="xxxxxx"


验签方式可自定义，只需要实现`com.biz.primus.base.session.filter.RquestVerifyFilter`接口，并且在SessionInterceptor创建中进行set。 
例:
```
@Configuration
public class WebAppConfig extends WebMvcConfigurationSupport   {  
  
	@Autowired
	private ISessionManage sessionManage;
    @Override  
    public void addInterceptors(InterceptorRegistry registry) {  
    	super.addInterceptors(registry);
        //注册自定义拦截器，添加拦截路径和排除拦截路径  
    	SessionInterceptor sessionInterceptor = new SessionInterceptor();
    	sessionInterceptor.setSessionManage(sessionManage);
    	sessionInterceptor.setRquestVerifyFilter(new RquestVerifyFilter() {
			
			@Override
			public boolean filter(HttpServletRequest request) {
				// 验签逻辑 返回true 验签成功
				return false;
			}
		});
        registry.addInterceptor(sessionInterceptor).addPathPatterns("/**").excludePathPatterns("/users/login","/error");  
    } 
}

```
`1.SessionUtil.VERIFY_SWITCH 增加一个公共静态属性开关，可以对验签进行开启和关闭 /r/n 2.  SessionUtil. DOMAIN_PATTERN 静态属性 可以进行设置会话信息cookie domain属性 /r/n 3.SeesionUtil.COOKIE_MAXAGE 静态属性可以设置会话信息cookie有效时间`

`我在使用postman进行验签测试的时候，遇到一个小坑，你在复制的时候postman里它可能会在你很长的字符中加入空格分割，并且如果你在开发环境中进行复制很长的字符时候，也许会复制到分割符，这些小问题也导致我浪费了一些多余时间`







### 关于Xss过滤配置与使用
Xss过滤分为3个部分 （需要过滤model的字段必须具有get and set 方法 ，否则不会进行过滤该字段）
1.对请求参数进行字符转义 
2.对返回数据进行字符转义
3.对返回视图，进行域对象过滤(目前过滤了session域和request域，applictionContext和page并没有进行过滤，也就是说如果是用户输入数据需要在context和page中取出， 则需要调用(<% SessionUitl.XSSHtmlFilt(String str)%>`这个转义字符串的方法。))
配置分为两个部分
1.对请求参数进行xss过滤，默认关闭。
开启方式为:
```
实现:`com.biz.primus.base.session.filter.XssFilter` 接口

@Configuration
public class WebAppConfig extends WebMvcConfigurationSupport   {  
  
    @Autowired
    private ISessionManage sessionManage;
    @Override  
    public void addInterceptors(InterceptorRegistry registry) {  
    	super.addInterceptors(registry);
        //注册自定义拦截器，添加拦截路径和排除拦截路径  
    	SessionInterceptor sessionInterceptor = new SessionInterceptor();
    	sessionInterceptor.setSessionManage(sessionManage);
    	//共享资源判断
//    	sessionInterceptor.setPublicResourceFilter(publicResourceFilter);
//    	验签方式
//    	sessionInterceptor.setRquestVerifyFilter(rquestVerifyFilter);
    	//xss判断
    	sessionInterceptor.setXssfilter(new XssFilter() {
			
			@Override
			public boolean filter(HttpServletRequest request) {
				
				// 返回true 则开启XSS字符转义，false则不开启，可以根据请求自定义自己的业务逻辑
				return true;
			}
		});
        registry.addInterceptor(sessionInterceptor).addPathPatterns("/**").excludePathPatterns("/login","/error");  
    } 

```

2.对返回数据或者视图进行字符转义，这个需要依赖AOP手动进行配置。 下面列一篇我测试成功的配置(主要处理方法就是SeesionUtil类中的dataHandle方法)
```
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
@Aspect
@Component
public class SpringAop {
	//需要使用切面的包路径
    @Pointcut("execution(* junqi.test.contrller.*.*(..))")
    public void qieru(){

    }
      @Around("qieru()")
    public Object huanrao(ProceedingJoinPoint poin) throws Throwable{
        Object object =  poin.proceed();
		return com.biz.primus.base.session.aop.SpringAop.dataHandle(poin, object);
    }
}
```

### 关于CRSF令牌配置与使用
`该令牌是为了防止脚本跨域攻击，当然在制作时候，由于验证了token会消耗掉，也就具有了一个防提交功能`


配置方式:
该令牌默认开启，如需要关闭，在创建com.biz.primus.base.session.sessionmanage.impl.SessionManageImpl时候需要调用
```
setDisable(false);
```
令牌时效默认30分钟，如需设置 ，在创建com.biz.primus.base.session.sessionmanage.impl.SessionManageImpl时候需要调用
```
//单位默认毫秒
setCsrf_disableTime(long time)
```
关于令牌对于什么请求进行验证(默认post方式提交全部验证 )，可自定义实现 `com.biz.primus.base.session.filter.CsrfTokenFilter`接口,将实现注入到session拦截器。
例:
```
@Configuration
public class WebAppConfig extends WebMvcConfigurationSupport   {  
  
    @Autowired
    private ISessionManage sessionManage;
    @Override  
    public void addInterceptors(InterceptorRegistry registry) {  
    	super.addInterceptors(registry);
        //注册自定义拦截器，添加拦截路径和排除拦截路径  
    	SessionInterceptor sessionInterceptor = new SessionInterceptor();
    	sessionInterceptor.setSessionManage(sessionManage);
    	sessionInterceptor.setCsrfTokenFilter(new CsrfTokenFilter() {
			
			@Override
			public boolean filter(HttpServletRequest request) {
				// 返回true则进行令牌验证，false则不需要
				return false;
			}
		});
        registry.addInterceptor(sessionInterceptor).addPathPatterns("/**").excludePathPatterns("/users/login","/error");  
    } 
}


```


使用方式：
前台访问需要令牌验证的接口必须携带令牌在header头中，形式:`csrf_token="xxxx"`.
令牌的获取，需要在之前的请求的头中携带 形式: `wantToken=1`(必须 等于1才有效) ，然后在响应头中会返回请求令牌 形式： `csrf_token="xxx"`
该令牌在请求结束后会被销毁掉，而且该令牌具有时效，超时失效，时效可配置。



### 关于SESSION会话的配置与使用
登录之前，可以访问公共资源，但是每次必须在header头携带未登录的会话标识 
例: Authorization = `a14372fd-206b-46c5-8055-18afc8d3594b`
"a14372fd-206b-46c5-8055-18afc8d3594b" 这是未登录的会话标识，未登录必须携带

登录成功，会在响应头返回一个真实的会话标识，后续的操作都必须携带真实会话标识。

`目前获取会话id有三种途径，优先级分别是head头、请求头参数携带、cookie获取` 

配置方式:
直接贴代码了 就只有两篇:
配置session依赖的缓存
```
package com.biz.primus.base.session.config;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.biz.primus.base.session.cache.impl.CacheAccesstoImpl;
import com.biz.primus.base.session.sessionmanage.ISessionManage;
import com.biz.primus.base.session.sessionmanage.impl.SessionManageImpl;

import redis.clients.jedis.JedisPoolConfig;
 
/**
 *
 * 集成RedisTemplate
 */
@Configuration
@EnableAutoConfiguration
public class RedisConfig {
 
    //获取springboot配置文件的值 (get的时候获取)
//    @Value("${spring.redis.hostName}")
    private String host;
 
//    @Value("${spring.redis.password}")
    private String password;
 
 
    @Bean
    @ConfigurationProperties(prefix = "spring.redis.pool")
    public JedisPoolConfig getRedisConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        return config;
    }
 
    @Bean
    @ConfigurationProperties(prefix = "spring.redis")
    public JedisConnectionFactory getConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setUsePool(true);
        JedisPoolConfig config = getRedisConfig();
        factory.setPoolConfig(config);
        return factory;
    }
 
    @Bean
    public RedisTemplate<String, ?> getRedisTemplate() {
        JedisConnectionFactory factory = getConnectionFactory();
        //因为我的redis 没有账号和密码 所以注释了
//        factory.setHostName(this.host);
//        factory.setPassword(this.password);
        RedisTemplate<String, ?> template = new StringRedisTemplate(getConnectionFactory());
        return template;
    }
    
    @Bean
    public ISessionManage CacheAccesstoImpl(@Autowired @Qualifier("getRedisTemplate") RedisTemplate<String, String> template) {
    	SessionManageImpl sessionManageImpl = new SessionManageImpl("com.biz.primus.base.session.controller");
    	sessionManageImpl.setCacheAccessto(new CacheAccesstoImpl(template));
    	//禁用CSRF令牌验证
    	sessionManageImpl.setDisable(true);
    	return sessionManageImpl;
	}
 
   
}

```
注册过滤器和拦截器:
```
/** 
 * 
 * 注册拦截器 
 */  
@Configuration
public class WebAppConfig extends WebMvcConfigurationSupport   {  
  
	@Autowired
	private ISessionManage sessionManage;
    @Override  
    public void addInterceptors(InterceptorRegistry registry) {  
    	super.addInterceptors(registry);
        //注册自定义拦截器，添加拦截路径和排除拦截路径  
    	SessionInterceptor sessionInterceptor = new SessionInterceptor();
    	sessionInterceptor.setSessionManage(sessionManage);
    	//共享资源判断 目前使用正则表达式来判定， 也可以取消注释进行自定义判定
//    	sessionInterceptor.setPublicResourceFilter(publicResourceFilter);
    	//验签逻辑
//    	sessionInterceptor.setRquestVerifyFilter(rquestVerifyFilter);
    	//xss过滤判断
//    	sessionInterceptor.setXssfilter(xssfilter);
        registry.addInterceptor(sessionInterceptor).addPathPatterns("/**").excludePathPatterns("/users/login","/error");  
    } 
    @Bean
    public CommonsMultipartResolver getCommonsMultipartResolver(){
    	CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    	commonsMultipartResolver.setMaxUploadSize(209715200);
    	commonsMultipartResolver.setDefaultEncoding("UTF-8");
    	commonsMultipartResolver.setResolveLazily(true);
    	return commonsMultipartResolver;
    }
    
//  配置过滤器  
    @Bean  
    public FilterRegistrationBean filterPageRegistration() {  
        FilterRegistrationBean registration = new FilterRegistrationBean();  
        registration.setFilter(new BizReqFilter());
        //设定匹配的路径
        registration.addUrlPatterns("/*");
        //设定名称
        registration.setName("bizReqFilter");
        //设定加载的顺序
        registration.setOrder(0);  
        return registration;  
    }
    
}
```

ok 配置结束，现在来进行使用  `会话中能使用的方法只有setAttribute()和getAttribute（如需存储对象请自行序列化为字符串，暂只支持字符串存储），因为目前并没有重写会话`。


供后台调用的其实只有3个方法 `login(...)`和`logout()和一个`isValid（String sessionId）`方法，主要是验证该会话是否有效。该方法主要是为了在不同的端登录之后可以共享不同端的会话信息 
例:
```
String sessionId = 从数据源获取；
if(isValid(sessionId)){
//使用原来的进行登录
   login(sessionId,....);
}else{
sessionId = 重新生成新的会话Id；
   login(sessionId,....);
}


```` 
直接贴代码



```

@Controller
public class TestController {
	@Autowired
	@Qualifier("getRedisTemplate") 
	private RedisTemplate<String, String> redisTemplate;
	@Autowired
	private ISessionManage sessionManage ;
	@RequestMapping("/users/login")
	@ResponseBody
	public String login(BizRequest request,HttpServletResponse response){

		System.err.println("登录成功");
		//尽量保证您的会话标识在分布式系统中不会重复
		String sessionId = UUID.randomUUID().toString();
		//7天
		sessionManage.login(sessionId, null, null, "604800", null);
		return "{\"code\": 0,\"msg\": \"success\",\"data\": {\"memberId\": 43}}";
	}
	
	@RequestMapping("/users/logout")
	@ResponseBody
	public String logout(BizRequest request,HttpServletResponse response){
		sessionManage.logout();
		return "退出成功";
	}


}
```

好了 使用讲完了 login方法参数意义 可以看接口说明
对了 在创建sessionManage，可以set一个callback ，这是会话生命周期重置的回调。 该会话管理中并不是每次访问都更新生命时长，而是由你设定的会话间隔来进行更新生命的，最快的更新是每次都进行更新。
就比如设置五分钟，五分钟到了其实并不会更新，这时候需要用户发起一个请求来激活本次更新。因为内部并没有存在定时检测的机制，而是依赖redis自带的key的有效期来实现。


###为了方便参考 我在加上一个我测试时候的appliction配置：
```
server:
    port: 8080

 
#spring-boot\u6574\u5408\u5355\u673A\u7248redis redis\u4F5C\u4E3A\u7F13\u5B58
spring.redis.hostName: 192.168.200.39
spring.redis.port: 6379
#spring.redis.password: 
spring.redis.timeout: 0
spring.redis.pool.max-active: 8
spring.redis.pool.max-wait: -1
spring.redis.pool.max-idle: 8
spring.redis.pool.min-idle: 0

```

# middleware-config
自动识别项目所属环境标识，以读取不同环境对应的配置文件。获取环境标识简单、准确、可靠。

环境标识
一、背景
我们的程序运行环境分线下环境、线上环境，线下环境线上环境又有细分。
 
线下环境	开发（本机）
	测试（项目测试，其他分支）
	稳定（回归测试，master）
 
线上环境	预发 （预发环境）
	正式 （正式环境）
	

有些环境是完全隔离的，也使用不同的数据库、缓存、MQ、配置中心，比如线下环境、线上环境之间。
有些环境是部分隔离的，使用相同的数据库、缓存、配置中心，但是使用不同的MQ，比如线上环境中的预发环境、正式环境。
有些环境是不隔离的，数据库、缓存、MQ、配置中心，可能相同也可能不同，通过一个中间件平台逻辑划分rpc调用关系，比如线下的三个环境。
 
线下环境、线上环境依赖不同的配置，我们针对不同环境有各自的配置。
通常使用如下几种方式来给不同环境添加相应的配置，
1.jenkins打包时从git配置库中拉取不同的配置文件打入jar包或war中，因为多套环境对应多套jenkins任务，jenkins能区分环境。
2.ip或域名类配置，配置文件或代码中统一写成某个域名，如zk1.wd.com,db.wd.com,diamond.wd.com,运维在相应环境的机器上/etc/hosts配置对应环境的ip。
3.借助配置中心diamond，将应用不同环境的配置放到对应环境的diamond中，diamond通过运维本地配置diamond.wd.com地址来区分环境。
 
以上方式有些场景需求满足不了，比如：
1.线上环境的预发环境和正式环境，他们使用相同的数据库、不同的MQ，对于异步从数据库中捞数据发送到MQ中去，会把正式环境产生的数据发送到预发的MQ中去，
相应的也会把预发的产生的数据发送到正式的MQ中去。
2.资金端想对同一个业务的预发环境、正式环境的执行结果进行比较，执行结果从数据库中捞取，目前无法区分哪笔是预发产生的、哪笔是正式环境产生的。
 
所以需要一个环境标识来区分预发环境、正式环境，上述场景只要把环境标识加到数据中，然后区分处理即可。
要求获取环境标识简单、准确、可靠。
 
应用系统可以加一个配置项来指定哪个ip的机器是预发的，这种方法可以解决眼前问题，但有弊端，
1.应用系统需要关心环境问题，并在系统中增加配置项，复杂度增加；
2.ip是可能变更的，比如迁移机器、ip地址重新规划等，做ip变更的是运维人员，而维护环境标识配置的是系统开发、产品人员，极大可能不同步。
 
经过讨论，我们倾向于运维人员在机器上维护环境标识。
 
线下环境	开发（本机） 标识：dev ，网段：192.168.0.0/16
	测试 （项目测试，其他分支）标识：test ，网段：172.20.100.0/32
	稳定（回归测试，master） 标识：stable，网段： 172.20.100.0/32
 
线上环境	预发 标识：pre
	正式 标识：prd
	
二、实现方案
运维同事在不同环境机器上增加配置文件 /root/public/environment ,其中包含env=dev/test/stable/pre/prd等键值对，中间件同事提供工具类获取环境标识。
为增加准确性，取到/root/public/environment环境标识后，还要验证机器主机名前缀（不同环境有固定前缀），验证环境对应的ip段，如果有一项不满足，则报错且启动失败。
既然不同环境机器有不同的固定主机名前缀，或固定的ip段，为什么不直接使用这些因素，原因还是准确性，主机名、ip段两个因素难保没有“出错”的时候，三因素互验证增加准确性；
另一个原因是配置文件方式扩展性强，以后其他标识也可放进去，主机名、ip段能表达信息有限。
 
Env枚举 
DEV("dev", "*", "192.168.0.0/16"),

TEST("test", "mdc-test-", "172.20.100.0/24"),

STABLE("stable", "mdc-stable-", "172.20.100.0/24"),

PRE("pre", "jf-pre-", "172.21.43.0/24"),

PRD("prd", "jf-prod-", "172.21.0.0/16,172.20.1.0/24,172.20.3.0/24,172.20.5.0/24,172.21.30.0/24,172.20.253.0/24");

 
ip段是可能变动的，比如增加或者重新规划，扩展性如何保证？
当这种情况发生时，我们提供三种方式：
1.升级工具类版本，这种方式是滞后的；
2.通过添加jvm参数"middleware.env=dev/test/stable/pre/prd"设置，该设置优先级最高，且不再验证主机名、ip段。
3.运维可对所有机器统一推送配置文件/root/public/environment.config ,工具类优先使用本配置文件中的主机名、ip端配置。
/root/public/environment.config 
dev=*;192.168.0.0/16
test=mdc-test-,wyr-test-;172.20.100.0/24,172.30.4.0/24
stable=mdc-test-,wyr-stable-;172.20.100.0/24,172.30.4.0/24
pre=jf-pre-;172.21.43.0/24
prd=jf-prd-;172.21.0.0/16,172.20.1.0/24,172.20.3.0/24,172.20.5.0/24,172.21.30.0/24,172.20.253.0/24
        注：1.*号匹配所有主机名，多个主机名前缀逗号分隔，多个ip段逗号分隔。
三、应用系统接入
1.引入依赖
<dependency>
    <groupId>com.weidai.middleware</groupId>
    <artifactId>middleware-config</artifactId>
    <version>1.0.1</version>
</dependency>
 
2.使用工具类MiddlewareEnv
静态方法	返回值
boolean isDev()	是否开发人员dev环境
boolean isTest() 	是否测试环境
boolean isStable() 	是否稳定环境
boolean isPre()	是否预发环境
boolean isPrd()	是否线上正式环境
String getEnvName() 	获取当前环境标识名，dev/test/stable/pre/prd
String getAttribute(String key)	获取/root/public/environment中其他配置项，
改方法优先从jvm参数中获取,对应的jvm参数为“middleware.”+key，
jvm参数没有则从/root/public/environment中获取
 
四、注意事项
(1)应用启动成功后，通过MiddlewareEnv获取环境标识出错怎么办？这是绝不能发生的，发生了后果很严重！
所以需要在启动主流程中校验环境标识的准确性，发现环境标识不正确的，及时停止应用，有如下方式可供选择：
1.如果使用了中间件组提供的可靠消息consistent-message等组件，可忽略这个问题，因为组件在初始化过程中已经校验了环境标识。
2.应用系统在启动主流程中主动调用MiddlewareEnv.validateInit() 方法，这个方法会校验环境标识，有问题题则会抛出异常，启动主流程校验点可以是springboot的启动类中，可以是非延迟加载的bean的afterpropertyset方法中，也可以是bean的static静态块中。
3.在jvm启动类中加上  -javaagent:middleware-config-1.0.1-SNAPSHOT.jar  ，在jvm预启动时会调用MiddlewareEnv.validateInit() 方法，middleware-config-1.0.1-SNAPSHOT.jar注意选择正确版本。
 
(2)日常、测试、线上环境运维同事可以统一配置/root/public/environment和主机名，开发人员的ide环境怎么办？
按照简单的原则，我们尽量不给开发人员增加负担，按照如下规则获取环境标识：
1.jvm参数middleware.env=dev/test/stable/pre/prd是否存在，存在则不关心/root/public/environment是否存在，该jvm参数不在则往下走
2./root/public/environment文件是否存在，若存在按照正常逻辑走；
2./root/public/environment文件若不存在，是否是192.168.0.0/16 ip段，若是统一归入dev环境；
3./root/public/environment文件不存在，且ip不在192.168.0.0/16段，则检查jvm参数middleware.developer=true是否存在，存在则认为是开发人员ide环境，归入dev环境；
 
简而言之，开发人员如果ip是192.168.0.0/16 段，不需要做任何配置，直接归入dev环境；
如果特殊情况不在这个ip段，有三种办法，
1.在jvm参数中加middleware.developer=true，归入dev环境；
2.在jvm参数中加middleware.env=dev/test/stable/pre/prd，直接指定环境；
3.在本机添加/root/public/environment配置文件

# 手写RPC

Created: December 22, 2022 1:29 PM

- 前言
    
             写这个目的是为了进行练习RPC的设计与实践，让自己对RPC这种目前常用的调用框架的理解更加深刻。本次主要实现两个基本功能：
    
    1. RPC服务的提供与调用；
    2. 支持同一服务的多个提供者进行多机部署。
    
- 整体架构图
    - 架构图
        
        ![Untitled](https://images.xianlingling.cn/jiagou.png)
        
        说明：
        
        1. 服务提供者启动时向注册中心进行服务的注册；
        2. 注册中心返回注册结果；
        3. 消费者启动时，向注册中心发送获取服务提供者请求；
        4. 注册中心返回服务提供者列表；
        5. 消费者通过获取的服务提供者列表，请求相应的服务。
- 关键设计
    - 协议设计
        
        ![Untitled](https://images.xianlingling.cn/xieyi.png)
        
        说明：
        
        1. 协议头包含两个字段，魔数和版本号；
        2. 协议体包含两种不同的协议，分别为RPC请求的协议体和请求响应的协议体；
        3. RPC请求协议体包含请求id，方法名、方法参数、方法参数值等。
        4. RPC响应协议体包含响应id，请求id，返回值等。
    - Provider设计
        - 流程图
            
            ![Untitled](https://images.xianlingling.cn/provider.png)
            
        - 加载Provider配置
            
            当程序启动时，首先要知道程序中有哪些Provider来提供服务，那么如何知道有哪些Provider呢？方法有很多种，比如把Provider信息事先写到一个XML的配置文件里（Dubbo的实现方式），通过注解进行声明的方式，JSON配置文件的方式等等，这里我们演示默认使用读取XML方式。
            
            - **DefaultProviderResourceReader**
                
                默认的Provider资源读取器，读取Provider的XML配置并转化为RocketProviderDefinition。
                
                ```java
                public class DefaultProviderResourceReader implements IRocketProviderResource {
                
                    /**
                     * 默认的provider配置文件名
                     */
                    private static final String RESOURCE = "rocket-producer.xml";
                
                    /**
                     * 默认的类型
                     */
                    private static final String TYPE = "provider";
                
                    /**
                     * provider所在的项目名
                     */
                    @Value("${roc.rocket.server.provider}")
                    private String provider;
                
                    /**
                     * 提供服务的端口
                     */
                    @Value("${roc.rocket.server.port}")
                    private Integer port;
                
                    @Autowired
                    private ApplicationContext applicationContext;
                
                    @Override
                    public List<RocketProviderDefinition> read() {
                        //读取XML配置内容
                        RocketProviderRootXmlConfig rocketProviderRootXmlConfig = readXml();
                        if (rocketProviderRootXmlConfig == null
                                || CollectionUtils.isEmpty(rocketProviderRootXmlConfig.getRocketProviderXmlConfigList())) {
                            return null;
                        }
                        List<RocketProviderDefinition> rocketProviderDefinitionList = Lists.newArrayList();
                        //解析XML配置内容，把XML配置的内容，转化为RocketProviderDefinition对象
                        for (RocketProviderXmlConfig rocketProviderXmlConfig : rocketProviderRootXmlConfig.getRocketProviderXmlConfigList()) {
                            if (TYPE.equals(rocketProviderXmlConfig.getType())) {
                                RocketProviderDefinition rocketProviderDefinition = new RocketProviderDefinition();
                                rocketProviderDefinition.setApiName(rocketProviderXmlConfig.getApi());
                                Object obj = applicationContext.getBean(rocketProviderXmlConfig.getRef());
                                rocketProviderDefinition.setClazz(obj.getClass());
                                rocketProviderDefinition.setInstance(obj);
                                rocketProviderDefinition.setProvider(provider);
                                rocketProviderDefinition.setPort(port);
                                rocketProviderDefinitionList.add(rocketProviderDefinition);
                            }
                
                        }
                        return rocketProviderDefinitionList;
                    }
                
                    /**
                     * 读取XML配置
                     *
                     * @return
                     */
                    private RocketProviderRootXmlConfig readXml() {
                        Resource resource = new ClassPathResource(RESOURCE);
                        BufferedReader br = null;
                        RocketProviderRootXmlConfig rocketProviderRootXmlConfig = null;
                        try {
                            br = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
                            StringBuffer buffer = new StringBuffer();
                            String line = "";
                            while ((line = br.readLine()) != null) {
                                buffer.append(line);
                            }
                            rocketProviderRootXmlConfig = XmlUtils.xmlToObject(RocketProviderRootXmlConfig.class, buffer.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return rocketProviderRootXmlConfig;
                      }
                    }
                ```
                
        - 启动Provider
            
            程序会通过启动Server类的start方法来初始化netty服务，从而提供Provider接收远程调用的方法。
            
            ```java
            public class Server {
            
                @Value("${roc.rocket.server.port}")
                private Integer port;
            
                /**
                 * 最大重试次数
                 */
                @Value("${roc.rocket.server.max-retry}")
                private Integer maxRetry;
            
                /**
                 * 是否正在启动服务
                 */
                private static Boolean starting = false;
                /**
                 * 服务是否启动完成
                 */
                private static Boolean started = false;
            
                private ServerBootstrap serverBootstrap;
            
                private EventLoopGroup bossGroup;
            
                private EventLoopGroup workGroup;
            
                private Channel channel;
            
                @Resource
                private ProviderChannelHandler providerChannelHandler;
            
                @PostConstruct
                public synchronized void start() {
                    if (!canStart()) {
                        return;
                    }
                    starting = true;
                    bossGroup = new NioEventLoopGroup();
                    workGroup = new NioEventLoopGroup();
                    serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(bossGroup, workGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline channelPipeline = ch.pipeline();
                                    channelPipeline.addLast(new RocketProtocolDecoder(new JsonSerializer()));
                                    channelPipeline.addLast(new RocketResponseEncoder(new JsonSerializer()));
                                    channelPipeline.addLast(providerChannelHandler);
            
                                }
                            });
                    try {
                        channel = start(serverBootstrap, port);
                    } catch (Exception e) {
                        log.error("rocket server exception :{}", e.getMessage());
                    }
                }
            
                private Channel start(ServerBootstrap serverBootstrap, int port) {
                    ChannelFuture channelFuture = serverBootstrap.bind(port).addListener(future -> {
                        if (future.isSuccess()) {
                            log.info("Rocket Server started , port is {}", port);
                            setStarted();
                        } else if (maxRetry == 0) {
                            throw new ServerStartFailException();
                        } else {
                            maxRetry--;
                            int delay = 1 << (maxRetry + 1);
                            serverBootstrap.config().group().schedule(() -> start(serverBootstrap, port), delay, TimeUnit.SECONDS);
                        }
                    });
                    return channelFuture.channel();
                }
            
                private Boolean canStart() {
                    return !(starting || started);
                }
            
                private void setStarted() {
                    starting = false;
                    started = true;
                }
            
                @PreDestroy
                private void shutdown() {
                    bossGroup.shutdownGracefully();
                    workGroup.shutdownGracefully();
                }
            
                public static void main(String[] args) {
                    Server server = new Server();
                    server.start();
                }
            }
            ```
            
        - 注册Provider到注册中心
            - **RocketProviderRegistry**
                
                注册provider到注册中心，最终是以ProviderRegistryExecutor提供的register方法注册到注册中心的
                
                ```java
                public abstract class RocketProviderRegistry {
                
                    private static Map<String, RocketProviderDefinition> rocketProducerDefinitionMap = Maps.newConcurrentMap();
                
                    @Resource
                    private RocketProviderResouceReader rocketProviderResouceReader;
                
                    @Resource
                    private ProviderRegistryExecutor providerRegistryExecutor;
                
                    @PostConstruct
                    private void init() {
                        //读取Provider配置信息
                        List<RocketProviderDefinition> producerDefinitionList = rocketProviderResouceReader.read();
                        //把Provider信息放入到缓存中
                        if (CollectionUtils.isNotEmpty(producerDefinitionList)) {
                            for (RocketProviderDefinition rocketProviderDefinition : producerDefinitionList) {
                                rocketProducerDefinitionMap.putIfAbsent(rocketProviderDefinition.getApiName(), rocketProviderDefinition);
                            }
                            //注册Provider信息到信息中心
                            providerRegistryExecutor.register(Lists.newArrayList(rocketProducerDefinitionMap.values()));
                        }
                    }
                
                    public RocketProviderDefinition getRocketProvider(String apiName) {
                        return rocketProducerDefinitionMap.get(apiName);
                    }
                  }
                ```
                
        - 执行RPC方法
            - **ProviderChannelHandler**
                - 执行PRC方法的入口，通过接收到的协议来进行解析，再通过RocketProviderExecutor.call来进行执行
                
                ```java
                public class ProviderChannelHandler extends SimpleChannelInboundHandler<RocketProtocol> {
                
                    @Resource
                    private RocketProviderExecutor rocketProducerExecutor;
                
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RocketProtocol rocketProtocol) throws Exception {
                        log.info("收到请求数据:{}", JsonUtils.toJson(rocketProtocol));
                        //解析协议体
                        RocketProtocolBody rocketProtocolBody = rocketProtocol.getRocketProtocolBody();
                        //执行调用
                        Object returnValue = rocketProducerExecutor.call(rocketProtocolBody.getFullClassName(), rocketProtocolBody.getMethodName(), rocketProtocolBody.getMethodValues());
                        log.info("返回数据：{}", JsonUtils.toJson(returnValue));
                        //封装请求响应协议
                        RocketResponseProtocol rocketResponseProtocol = new RocketResponseProtocol();
                        rocketResponseProtocol.setId(UuidUtils.createId());
                        rocketResponseProtocol.setRequestId(rocketProtocol.getId());
                        rocketResponseProtocol.setResponse(returnValue);
                        channelHandlerContext.writeAndFlush(rocketResponseProtocol);
                    }
                
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        log.info("客户端{}已断开", ctx.channel().id());
                        super.channelInactive(ctx);
                    }
                }
                ```
                
            - **RocketProviderExecutor**
                
                ```java
                public class RocketProviderExecutor extends RocketProviderRegistry {
                
                    public Object call(String api, String methodName, Object[] values) throws Exception {
                        //通过调用api获取Provider信息
                        RocketProviderDefinition rocketProviderDefinition = getRocketProvider(api);
                        if (rocketProviderDefinition == null) {
                            throw new NoSuchProviderException();
                        }
                        Method[] methods = rocketProviderDefinition.getClazz().getMethods();
                        //找到执行方法
                        Method method = findMethod(methods, methodName);
                        if (method == null) {
                            throw new NoSuchMethodException();
                        }
                        //通过反射执行并返回结果
                        return method.invoke(rocketProviderDefinition.getInstance(), values);
                    }
                
                    private Method findMethod(Method[] methods, String methodName) {
                        for (Method m : methods) {
                            if (m.getName().equals(methodName)) {
                                return m;
                            }
                        }
                        return null;
                    }
                
                }
                ```
                
    - Comsumer设计
        - 流程图
            
            ![Untitled](https://images.xianlingling.cn/consumer.png)
            
        - 代理类的生成与注册
            - **RocketConsumerApiRegistry**
                - 我们想像使用普通的类一样，直接通过@Resource注解来拿到想要使用的类并执行某个方法去远程调用的接口，那么我们需要做两步：
                    1. 把远程调用API的管理注册到Spring容器中
                    2. 通过代理的方式对执行方法进行拦截，去调用远程的服务，然后返回执行结果
                - RocketConsumerApiRegistry继承了BeanDefinitionRegistryPostProcessor接口，实现了postProcessBeanDefinitionRegistry方法，从而实现了第一步的需求。
                    
                    ```java
                    public class RocketConsumerApiRegistry implements BeanDefinitionRegistryPostProcessor {
                    
                        @Override
                        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
                            RocketConsumerResouceReader rocketConsumerResouceReader = new RocketConsumerResouceReader(new DefaultRocketConsumerResourceReader());
                            //读取consumer配置，这里也采用XML配置文件方式
                            List<RocketConsumerDefinition> rocketConsumerDefinitionList = rocketConsumerResouceReader.read();
                            if (CollectionUtils.isNotEmpty(rocketConsumerDefinitionList)) {
                                for (RocketConsumerDefinition rocketConsumerDefinition : rocketConsumerDefinitionList) {
                                    try {
                                        Class<?> cls = Class.forName(rocketConsumerDefinition.getApi());
                                        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(cls);
                                        //生成注册bean需要的bean definition
                                        GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinitionBuilder.getRawBeanDefinition();
                                        definition.getPropertyValues().add("clazz", cls);
                                        definition.getPropertyValues().add("api", rocketConsumerDefinition.getApi());
                                        //生成代理类的关键
                                        definition.setBeanClass(RocketRpcProxyFactory.class);
                                        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                                        // 注册bean名,一般为类名首字母小写
                                        beanDefinitionRegistry.registerBeanDefinition(rocketConsumerDefinition.getId(), definition);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    
                        @Override
                        public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
                    
                        }
                    
                    }
                    ```
                    
            - **RocketRpcProxyFactory**
                - RocketRpc的类工厂，通过继承FactoryBean来生成bean
                
                ```java
                public class RocketRpcProxyFactory<T> implements FactoryBean<T> {
                
                    private Class<?> clazz;
                
                    private String api;
                
                    @Resource
                    private ApplicationContext applicationContext;
                
                    @Override
                    public T getObject() throws Exception {
                        RocketRpcProxy rocketRpcProxy = applicationContext.getBean(RocketRpcProxy.class);
                        return rocketRpcProxy.create(clazz, api);
                    }
                
                    @Override
                    public Class<?> getObjectType() {
                        return clazz;
                    }
                
                    @Override
                    public boolean isSingleton() {
                        return true;
                    }
                
                }
                ```
                
            - **RpcProxy**
                - Rpc的代理类，远程调用的方法最终在这里进行调用和返回
                
                ```java
                public class RpcProxy implements InvocationHandler {
                
                    private Class<?> clazz;
                
                    private String api;
                
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (proxy.getClass().equals(method.getDeclaringClass())) {
                            return method.invoke(proxy, args);
                        } else {
                            return rpcInvoke(proxy, method, args);
                        }
                    }
                
                    private Object rpcInvoke(Object proxy, Method method, Object[] args) throws Exception {
                        //生成协议体
                        RocketProtocolBody rocketProtocolBody = new RocketProtocolBody();
                        rocketProtocolBody.setFullClassName(this.clazz.getName());
                        rocketProtocolBody.setMethodName(method.getName());
                        rocketProtocolBody.setMethodValues(args);
                        RocketProtocol rocketProtocol = new RocketProtocol(UuidUtils.createId(), rocketProtocolBody);
                        RpcExecutor rpcExecutor = ApplicationContextUtils.getBean(RpcExecutor.class);
                        //执行远程调用方法
                        return rpcExecutor.call(rocketProtocol, api);
                    }
                }
                ```
                
        - 远程调用
            - **RocketConsumerClientManager**
                - 负责远程通讯的client的管理，利用initProviders方法初始化与提供服务的provider的连接
                
                ```java
                private void initProviders() {
                        RocketConsumerResouceReader rocketConsumerResouceReader = new RocketConsumerResouceReader(new DefaultRocketConsumerResourceReader());
                        //读取comsumer配置
                        List<RocketConsumerDefinition> rocketConsumerDefinitionList = rocketConsumerResouceReader.read();
                        if (CollectionUtils.isNotEmpty(rocketConsumerDefinitionList)) {
                            Map<String, List<Client>> serverMap = Maps.newHashMap();
                            for (RocketConsumerDefinition rocketConsumerDefinition : rocketConsumerDefinitionList) {
                                //通过api匹配注册中心提供服务的provider
                                List<Provider> providers = providerRegistryExecutor.getProvider(rocketConsumerDefinition.getApi());
                                if (CollectionUtils.isNotEmpty(providers)) {
                                    Provider provider = Iterables.getFirst(providers, null);
                                    if (provider == null) {
                                        continue;
                                    }
                                    //生成client，连接provider
                                    if (!serverMap.containsKey(provider.getProvider())) {
                                        List<Client> clients = providers.stream().map(p -> buildConsumerClient(p)).collect(Collectors.toList());
                                        serverMap.put(provider.getProvider(), clients);
                                    }
                                    consumerClientMap.put(rocketConsumerDefinition.getApi(), serverMap.get(provider.getProvider()));
                                }
                            }
                        }
                    }
                ```
                
            - **Client**
                - 负责与provider进行通信，并对返回的结果进行处理
                - Client.call()
                    
                    ```java
                    public Object call(RocketProtocol rocketProtocol) throws Exception {
                            //判断连接是否准备好
                            if (isClientReady()) {
                                channel.writeAndFlush(rocketProtocol);
                                //返回响应结果
                                return ConsumerMethodInboundHandler.getResponse(rocketProtocol.getId());
                            }
                            return null;
                        }
                    
                    public Boolean isClientReady() throws Exception {
                            //客户端进行远程连接，如果正在连接或已经连接成功，则忽略
                            this.connect();
                            //如果客户端没有连接成功，会自动进行重试，所以这里只判断是否连接成功
                            // ，如果没有并且客户端还在重试，则进行循环等待
                            while (!isConnection() && !isConnectionServerError()) {
                                TimeUnit.SECONDS.sleep(1);
                            }
                            //如果重试后仍然失败，则抛出连接异常
                            if (isConnectionServerError()) {
                                throw new ClientConnectionTimeoutException();
                            }
                            return true;
                        }
                    ```
                    
                - ConsumerMethodInboundHandler.getResponse()
                    - 由于远程调用的响应结果是异步的，所以需要设计一个异步的响应框架来对响应结果进行处理，从而使使用者感觉到还是同步的。
                    
                    ```java
                    public static Object getResponse(String requestId) {
                            //每个请求都有唯一的一个requestId，通过isReady来判断请求是否准备好，即是否被发送成功
                            if (!isReady(requestId)) {
                                throw new ResponseTimeoutException();
                            }
                    
                            try {
                                ResponseFuture responseFuture = requestMap.get(requestId);
                                //等待返回调用结果
                                RocketResponseProtocol responseProtocol = responseFuture.getRocketResponse(RESPONSE_TIMEOUT);
                                if (responseProtocol == null) {
                                    return null;
                                }
                                return responseProtocol.getResponse();
                            } finally {
                                requestMap.remove(requestId);
                            }
                        }
                    ```

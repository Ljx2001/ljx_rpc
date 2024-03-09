package com.ljx.config;

import com.ljx.IdGenerator;
import com.ljx.ProtocolConfig;
import com.ljx.compress.Compressor;
import com.ljx.compress.impl.GzipCompressor;
import com.ljx.discovery.RegistryConfig;
import com.ljx.loadbalancer.LoadBalancer;
import com.ljx.loadbalancer.impl.MinimumResponseTimeLoadBalancer;
import com.ljx.loadbalancer.impl.RoundRobinLoadBalancer;
import com.ljx.serialize.Serializer;
import com.ljx.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局配置类，代码配置->xml配置->默认项
 * @Author LiuJixing
 * @Date 8/3/2024
 */
@Data
@Slf4j
public class Configuration {
    //配置信息-->端口
    private int port = 8089;
    //配置信息-->应用程序名称
    private String applicationName = "default";
    //配置信息-->序列化方式
    private String serializeType = "jdk";
    //配置信息-->压缩方式
    private String compressType = "gzip";
    //配置信息-->注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    //配置信息-->Id生成器
    private IdGenerator idGenerator = new IdGenerator(Long.valueOf(1),Long.valueOf(2));
    //配置信息-->负载均衡器
    private LoadBalancer LoadBalancer = new MinimumResponseTimeLoadBalancer();

    //读xml
    public Configuration(){
        //成员变量的默认配置项

        //spi机制发现相关配置项
        SpiResolver spiResover = new SpiResolver();
        spiResover.loadFromSpi(this);

        //读取xml获得以上配置信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        //编程配置项，由RpcBootstrap提供
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}

package com.ljx.config;

import com.ljx.IdGenerator;
import com.ljx.ProtocolConfig;
import com.ljx.compress.Compressor;
import com.ljx.compress.CompressorFactory;
import com.ljx.discovery.RegistryConfig;
import com.ljx.serialize.Serializer;
import com.ljx.serialize.SerializerFactory;
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
 * @Author LiuJixing
 * @Date 8/3/2024
 */
@Slf4j
public class XmlResolver {
    public void loadFromXml(Configuration configuration) {
        try {
            //读取配置文件
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
            Document doc = builder.parse(inputStream);
            //获取一个xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            //解析表达式
            configuration.setPort(rosolvePort(doc,xPath));
            configuration.setApplicationName(resolveApplicationName(doc,xPath));
            configuration.setRegistryConfig(new RegistryConfig(resovleRegistry(doc,xPath)));

            configuration.setIdGenerator(resolveIdGenerator(doc,xPath));

            configuration.setCompressType(resolveCompressType(doc,xPath));
            configuration.setSerializeType(resolveSerializeType(doc,xPath));

            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(doc,xPath);
            CompressorFactory.addCompressor(compressorObjectWrapper);

            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(doc,xPath);
            SerializerFactory.addSerializer(serializerObjectWrapper);

            configuration.setLoadBalancer(resolveLoadBalancer(doc,xPath));

            System.out.println("配置信息："+configuration);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            log.debug("未读到xml配置文件，使用默认配置");
        }
        //代码配置由引导程序完成
    }

    /**
     * 解析压缩方式
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return 压缩方式
     */
    private String resolveCompressType(Document doc, XPath xPath) {
        String expression = "/configuration/compressType";
        return parseString(doc, xPath, expression);
    }
    private ObjectWrapper<Compressor> resolveCompressor(Document doc, XPath xPath) {
        String expression = "/configuration/compressor";
        Compressor compressor = parseObject(doc, xPath, expression, null);
        Byte code =Byte.valueOf(parseString(doc, xPath, expression,"code"));
        String name = parseString(doc, xPath, expression,"name");
        ObjectWrapper<Compressor> compressorObjectWrapper = new ObjectWrapper<>(code, name, compressor);
        return compressorObjectWrapper;
    }

    /**
     * 解析负载均衡器
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return 负载均衡器
     */
    private com.ljx.loadbalancer.LoadBalancer resolveLoadBalancer(Document doc, XPath xPath) {
        String expression = "/configuration/loadBalancer";
        return parseObject(doc, xPath, expression, new Class[]{}, new Object[]{});
    }

    /**
     * 解析序列化方式
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return 序列化方式
     */
    private String resolveSerializeType(Document doc, XPath xPath) {
        String expression = "/configuration/serializeType";
        return parseString(doc, xPath, expression);
    }
    private ObjectWrapper<Serializer> resolveSerializer(Document doc, XPath xPath) {
        String expression = "/configuration/serializer";
        Serializer serializer = parseObject(doc, xPath, expression,null);
        Byte code = Byte.valueOf(parseString(doc, xPath, expression,"code"));
        String name = parseString(doc, xPath, expression,"name");
        return new ObjectWrapper<>(code, name, serializer);
    }

    /**
     * 解析Id生成器
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return Id生成器
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xPath) {
        String expression = "/configuration/idGenerator";
        long dataCenterId = Long.parseLong(parseString(doc, xPath, expression,"dataCenterId"));
        long machineId = Long.parseLong(parseString(doc, xPath, expression,"machineId"));
        IdGenerator Generator = parseObject(doc, xPath, expression,new Class[]{Long.class, Long.class},dataCenterId,machineId);
        return Generator;
    }

    /**
     * 解析注册中心信息
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return 注册中心信息
     */
    private String resovleRegistry(Document doc, XPath xPath) {
        String expression = "/configuration/registry";
        return parseString(doc, xPath, expression);
    }

    /**
     * 解析应用名
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return 应用名
     */
    private String resolveApplicationName(Document doc, XPath xPath) {
        String expression = "/configuration/applicationName";
        return parseString(doc, xPath, expression);
    }

    /**
     * 解析端口号
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @return int 端口号
     */
    private int rosolvePort(Document doc, XPath xPath) {
        String expression = "/configuration/port";
        return Integer.parseInt(parseString(doc, xPath, expression));
    }

    /**
     * 解析一个节点，返回对应的实例
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @param expression 节点表达式
     * @param paramType 参数类型
     * @param param 参数
     * @return 实例
     * @param <T> 实例类型
     */
    private <T> T parseObject(Document doc, XPath xPath,String expression,Class[] paramType,Object... param){
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            Node node = targetNode.getAttributes().getNamedItem("class");
            String classname = node.getNodeValue();
            Class<?> aClass = Class.forName(classname);
            Object instance = null;
            if(paramType==null) {
                instance = aClass.getConstructor(paramType).newInstance();
            } else {
                instance = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | XPathExpressionException e) {
            log.debug("解析表达式时发生问题",e);
        }
        return null;
    }
    /**
     * 解析一个节点，返回对应的属性值 <port number="7777"/>
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @param expression 节点表达式
     * @param AttributeName 属性名
     * @return 属性值字符串
     */
    private String parseString(Document doc, XPath xPath,String expression,String AttributeName){
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            Node node = targetNode.getAttributes().getNamedItem(AttributeName);
            return node.getNodeValue();
        } catch (XPathExpressionException e) {
            log.debug("解析表达式时发生问题",e);
        }
        return null;
    }
    /**
     * 解析一个节点，返回对应的文本 <port>7777</port>
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @param expression 节点表达式
     * @return 属性值字符串
     */
    private String parseString(Document doc, XPath xPath,String expression){
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.debug("解析表达式时发生问题",e);
        }
        return null;
    }
}

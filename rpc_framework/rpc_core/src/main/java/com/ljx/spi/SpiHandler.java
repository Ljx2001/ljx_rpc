package com.ljx.spi;

import com.ljx.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixing
 * @Date 8/3/2024
 */
@Slf4j
public class SpiHandler {
    //定义一个basePath
    private static final String basePath = "META-INF/rpc-services";
    //定义一个缓存，保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);
    //缓存每一个接口所对应的实现的实例
    private static final Map<Class, List<Object>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);
    //加载当前类后，需要将spi保存，避免运行时频繁IO
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(basePath);
        if(fileUrl != null){
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if(children != null && children.length>0)
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
        }
    }

    /**
     * 获取第一个和clazz对应的spi实现
     * @param clazz 一个服务接口的class
     * @return 实现类的第一个实例
     * @param <T>
     */
    public static <T> T get(Class<T> clazz) {
        //优先从缓存里面获取
        List<Object> impls = SPI_IMPLEMENT.get(clazz);
        if(impls != null && !impls.isEmpty()){
            return (T) impls.get(0);
        }
        //如果缓存里面没有，就要建立缓存
        buildCache(clazz);
        //再次尝试获取第一个
        return (T) SPI_IMPLEMENT.get(clazz).get(0);
    }
    /**
     * 获取所有和clazz对应的spi实现
     * @param clazz 一个服务接口的class
     * @return 实现类的实例集合
     * @param <T>
     */
    public static <T> List<T> getList(Class<T> clazz) {
        //优先从缓存里面获取
        List<Object> impls = SPI_IMPLEMENT.get(clazz);
        if(impls != null && !impls.isEmpty()){
            return (List<T>)impls;
        }
        //如果缓存里面没有，就要建立缓存
        buildCache(clazz);
        return (List<T>)SPI_IMPLEMENT.get(clazz);
    }

    /**
     * 构架clazz相关的缓存
     * @param clazz 接口
     */
    private static void buildCache(Class<?> clazz) {
        String name = clazz.getName();
        List<Object> implement = new ArrayList<>();
        List<String> implNames = SPI_CONTENT.get(name);
        //实例化所有实现
        if(implNames != null && !implNames.isEmpty()){
            for (String implName : implNames) {
                try{
                    Class<?> aClass = Class.forName(implName);
                    Object impl = aClass.getConstructor().newInstance();
                    implement.add(impl);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e){
                    log.debug("spi实例化【{}】的实现时发生异常",implName);
                }
            }
        }
        SPI_IMPLEMENT.put(clazz, implement);
    }

    /**
     * 获取文件内所有的实现名称
     * @param child 文件对象
     * @return 文件对应的接口服务的所有实现类的全限定名
     */
    private static List<String> getImplNames(File child){
        try(
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        )
        {
            List<String> implNames = new ArrayList<>();
            while (true){
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)){
                    break;
                }
                implNames.add(line);
            }
            return implNames;
        } catch (IOException e){
            log.debug("读取spi文件时发生异常",e);
        }
        return null;
    }

    public static void main(String[] args) {

    }
}

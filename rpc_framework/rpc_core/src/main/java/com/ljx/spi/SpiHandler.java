package com.ljx.spi;

import com.ljx.Exceptions.SpiException;
import com.ljx.config.ObjectWrapper;
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
import java.util.stream.Collectors;

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
    private static final Map<Class, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);
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
    public synchronized static <T> ObjectWrapper<T> get(Class<T> clazz) {
        //优先从缓存里面获取
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && !objectWrappers.isEmpty()){
            return (ObjectWrapper<T>) objectWrappers.get(0);
        }
        //如果缓存里面没有，就要建立缓存
        buildCache(clazz);
        //再次尝试获取第一个
        List<ObjectWrapper<?>> results = SPI_IMPLEMENT.get(clazz);
        if(results == null || results.isEmpty()){
            return null;
        }
        return (ObjectWrapper<T>) results.get(0);
    }
    /**
     * 获取所有和clazz对应的spi实现
     * @param clazz 一个服务接口的class
     * @return 实现类的实例集合
     * @param <T>
     */
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {
        //优先从缓存里面获取
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && !objectWrappers.isEmpty()){
            return objectWrappers.stream().map(e->(ObjectWrapper<T>)e).collect(Collectors.toList());
        }
        //如果缓存里面没有，就要建立缓存
        buildCache(clazz);
        objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && !objectWrappers.isEmpty()){
            return objectWrappers.stream().map(e->(ObjectWrapper<T>)e).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 构架clazz相关的缓存
     * @param clazz 接口
     */
    private static void buildCache(Class<?> clazz) {
        String name = clazz.getName();
        List<ObjectWrapper<?>> implement = new ArrayList<>();
        List<String> implNames = SPI_CONTENT.get(name);
        if(implNames == null || implNames.isEmpty()){
            log.debug("spi文件中没有找到【{}】的实现",name);
            return;
        }
        //实例化所有实现
        if(implNames != null && !implNames.isEmpty()){
            for (String implName : implNames) {
                try{
                    String[] codeAndTypeAndName = implName.split("-");
                    if(codeAndTypeAndName.length != 3){
                        log.debug("spi文件中【{}】的实现格式不正确",implName);
                        throw new SpiException("spi文件中格式不正确");
                    }
                    Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                    String type = codeAndTypeAndName[1];
                    String implementName = codeAndTypeAndName[2];
                    Class<?> aClass = Class.forName(implementName);
                    Object impl = aClass.getConstructor().newInstance();
                    ObjectWrapper implWrapper = new ObjectWrapper(code, type , impl);
                    implement.add(implWrapper);
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

package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.misc.SXElement;
import com.jiuqi.dna.core.spi.publish.BundleToken;
import com.jiuqi.dna.core.spi.publish.NamedFactory;
import com.jiuqi.dna.core.spi.publish.NamedFactoryElement;


public class Request1ServicesGather extends NamedFactory<IRequest1Service,GMTServiceElement>{

	private Map<String,IRequest1Service> rServicesMap = new HashMap<String,IRequest1Service>();
	private List<IRequest1Service> serviceList = new ArrayList<IRequest1Service>();
	
	static Request1ServicesGather instance;

	private Request1ServicesGather() {
		instance = this;
	}
	
	@Override
	protected IRequest1Service doNewElement(Context context, GMTServiceElement meta,
			Object... adArgs) {
		return newElement(meta.serviceClazz, context, adArgs);
	}

	@Override
	protected GMTServiceElement parseElement(SXElement element, BundleToken bundle)
			throws Throwable {
		String name = element.getAttribute("name");
		String clazzName = element.getAttribute("class");
		GMTServiceElement serviceElement = new GMTServiceElement(element, bundle);
		IRequest1Service sb = serviceElement.serviceClazz.newInstance();
		
		serviceList.add(sb);
		rServicesMap.put(name, sb);
		return serviceElement;
	}
	
	public static Map<String,IRequest1Service> getServicesMap() {
		return instance.rServicesMap;
	}
	
	public static List<IRequest1Service> getServiceList() {
		return instance.serviceList;
	}
}
class GMTServiceElement extends NamedFactoryElement {
	
	Class<IRequest1Service> serviceClazz;


	public GMTServiceElement(SXElement element, BundleToken bundle)
			throws ClassNotFoundException {
		super(element.getAttribute("class"));
		serviceClazz = bundle.loadClass(element.getAttribute("class"),
				IRequest1Service.class);
	}
	
}

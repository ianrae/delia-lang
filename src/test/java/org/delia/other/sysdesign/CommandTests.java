package org.delia.other.sysdesign;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CommandTests {
	
	public static class Product {
		private String name;
		private Double price;
		private boolean onSale;
		
		public Product(String name, Double price, boolean onSale) {
			this.name = name;
			this.price = price;
			this.onSale = onSale;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public boolean isOnSale() {
			return onSale;
		}
		public void setOnSale(boolean onSale) {
			this.onSale = onSale;
		}
	}
	
	public class OrderDao {
	    private List<Product> products = new ArrayList<>();
	    
	    public void addToOrder(Product prod) {
	    	products.add(prod);
	    }
	    
	    public List<Product> findProducts(double minPrice, double maxPrice, boolean onSale) {
	    	List<Product> list = new ArrayList<>();
	    	for(Product prod: products) {
	    		if (prod.getPrice() >= minPrice && prod.getPrice() <= maxPrice) {
	    			if (prod.isOnSale() == onSale) {
	    				list.add(prod);
	    			}
	    		}
	    	}
	    	
	    	return list;
	    }
	}
	
	public class OrderService {
	    private OrderDao orderDao;
	    
	    public OrderService(OrderDao dao) {
	    	this.orderDao = dao;
	    }
	    
	    public List<Product> findProducts(double minPrice, double maxPrice) {
	    	return orderDao.findProducts(minPrice, maxPrice, false);
	    }
	}
	
	public class OrderFacade {
	    private OrderService orderService;
	    
	    public OrderFacade(OrderService orderService) {
	    	this.orderService = orderService;
	    }
	    
	    public List<Product> findProducts(double minPrice, double maxPrice) {
	    	return orderService.findProducts(minPrice, maxPrice);
	    }
	}
	
	public class OrderController {
	    private OrderFacade orderFacade;
	    
	    public OrderController(OrderFacade orderFacade) {
	    	this.orderFacade = orderFacade;
	    }
	    
	    public List<Product> findProducts(double minPrice, double maxPrice) {
	    	return orderFacade.findProducts(minPrice, maxPrice);
	    }
	}
	
	@Test
	public void test() {
		OrderController controller = createController();
		
		List<Product> list = controller.findProducts(3.0, 8.0);
		assertEquals(2, list.size());
	}

	private OrderController createController() {
		OrderDao dao = new OrderDao();
		Product prod = new Product("milk", 4.0, false);
		dao.addToOrder(prod);
		prod = new Product("bread", 2.0, false);
		dao.addToOrder(prod);
		prod = new Product("chicken", 6.0, false);
		dao.addToOrder(prod);
		prod = new Product("cheese", 4.0, true);
		dao.addToOrder(prod);
		
		OrderService orderService = new OrderService(dao);
		OrderFacade orderFacade = new OrderFacade(orderService);
		OrderController controller = new OrderController(orderFacade);
		return controller;
	}
}

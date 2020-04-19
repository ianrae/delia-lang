package org.delia.other.sysdesign;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class Strategy0Tests {
	
	public enum Region {
		QUEBEC,
		CANADA_OTHER,
		TEXAS,
		USA_OTHER
	};
	
	public static class Product {
		private String name;
		private Double price;
		private boolean isShippingTax;
		
		public Product(String name, Double price) {
			this.name = name;
			this.price = price;
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
		public boolean isShippingTax() {
			return isShippingTax;
		}
		public void setShippingTax(boolean isShippingTax) {
			this.isShippingTax = isShippingTax;
		}
	}
	
	public class OrderService {
	    private List<Product> products = new ArrayList<>();
	    
	    public void addToOrder(Product prod) {
	    	products.add(prod);
	    }
	    
	    public Double recalculateOrder(Region region) {
	    	double total = 0.0;
	    	for(Product prod: products) {
	    		double tax = getTax(prod, region);
	    		total += prod.price;
	    		total += tax;
	    		total += getShippingTax(prod, region);
	    	}
	    	
	    	return total;
	    }

		private double getTax(Product prod, Region region) {
			switch(region) {
			case QUEBEC:
				return prod.price * 0.10;
			case CANADA_OTHER:
				return prod.price * 0.20;
			case TEXAS:
				return 0.0;
			case USA_OTHER:
				return prod.price * 0.05;
			default:
				return 0.0;
			}
		}
		private double getShippingTax(Product prod, Region region) {
			switch(region) {
			case QUEBEC:
			case CANADA_OTHER:
				return prod.isShippingTax() ? 1.0 : 0.0;
			case TEXAS:
			case USA_OTHER:
			default:
				return 0.0;
			}
		}
	 
	}
	
	@Test
	public void test() {
		OrderService orderService = new OrderService();
		addProducts(orderService);
		
		Region region = Region.QUEBEC;
		Double total = orderService.recalculateOrder(region);
		assertEquals(18.6, total, 0.0001);
	}

	private void addProducts(OrderService orderService) {
		Product prod = new Product("milk", 4.0);
		prod.setShippingTax(true);
		orderService.addToOrder(prod);
		prod = new Product("bread", 2.0);
		orderService.addToOrder(prod);
		prod = new Product("chicken", 10.0);
		orderService.addToOrder(prod);
	}
}

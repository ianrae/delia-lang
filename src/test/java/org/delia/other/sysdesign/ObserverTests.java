package org.delia.other.sysdesign;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ObserverTests {
	
	public interface Channel {
	    public void update(String o);
	}
	
	public class NewsAgency {
	    private String news;
	    private List<Channel> channels = new ArrayList<>();
	 
	    public void addObserver(Channel channel) {
	        this.channels.add(channel);
	    }
	 
	    public void removeObserver(Channel channel) {
	        this.channels.remove(channel);
	    }
	 
	    public void setNews(String news) {
	        this.news = news;
	        
	        //if we used explicit notification
	        //customerService.notify(news);
	        //radioStationService.notify(news);
	        //advertisingDepartmentService.notify(news);
	        
	        for (Channel channel : this.channels) {
	            channel.update(this.news);
	        }
	    }
	}
	
	public class NewsChannel implements Channel {
	    private String news;
	 
	    @Override
	    public void update(String news) {
	        this.setNews(news);
	        System.out.println("got some news: " + news);
	    }

		private void setNews(String news2) {
			this.news = news2;
		}

		public String getNews() {
			return news;
		} 
	}	
	
	
	@Test
	public void test() {
		NewsAgency observable = new NewsAgency();
		NewsChannel observer = new NewsChannel();
		 
		observable.addObserver(observer);
		String newsItem = "The Maple Leafs won!";
		observable.setNews(newsItem);
		assertEquals(observer.getNews(), newsItem);		 
	}
}

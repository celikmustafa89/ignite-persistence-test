package com.celikmustafa.cache.persistencetest;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.TransactionException;
import org.springframework.stereotype.Service;

import com.yahoo.labs.samoa.instances.DenseInstance;

import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.clusterers.streamkm.StreamKM;

@Service
public class CacheService {

	private Ignite ignite;

	@PostConstruct
	public void init() {

		Ignite ignite = Ignition
				.start("C:\\Dev\\JavaProjects\\persistence-test\\src\\main\\resources\\ignite-config.xml");
		ignite.cluster().active(true);
		this.ignite = ignite;

		IgniteCache<Object, Object> cache = ignite.getOrCreateCache("cache2");
		// ignite.destroyCache("cache1");
		// this.test_get(cache, "key3");
		try {
			// this.trainAndSave(cache);
			this.test_get(cache, "moamodel");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void test_add(IgniteCache<Object, Object> cache, String key, String value) {
		try {
			cache.put(key, value);
		} catch (TransactionException e) {
			e.printStackTrace();
		}

	}

	private void trainAndSave(IgniteCache<Object, Object> cache) throws Exception {
		StreamKM streamKM = new StreamKM();
		// for start
		streamKM.numClustersOption.setValue(2);
		streamKM.evaluateOption.setValue(true);
		streamKM.resetLearning();
		for (int i = 0; i < 100000; i++) {
			Random rand = new Random();
			double[] features = new double[] { rand.nextDouble(), rand.nextDouble(), rand.nextDouble(),
					rand.nextDouble() };
			DenseInstance denseInstance = new DenseInstance(1.0, features);
			streamKM.trainOnInstanceImpl(denseInstance);
		}

		// for end
		Clustering clusteringResult = streamKM.getClusteringResult();
		int i = 1;
		System.out.println(clusteringResult.size());
		for (Cluster cluster : clusteringResult.getClustering()) {
			cluster.setId(i);
			i++;
			String info = cluster.getInfo();
			double[] center = cluster.getCenter();
			StringBuilder centerPoints = new StringBuilder();
			for (double point : center) {
				centerPoints.append(point).append(", ");
			}
			System.out.println("Center Points: {}" + centerPoints.substring(0, centerPoints.length() - 2));
		}

		cache.put("moamodel", streamKM);
		// streamKM.manager.clustererRandom = new MTRandom(1);
		// tekrar outputlara bakılacak. eşleşiyorsa işlem tamam.
	}

	private void test_get(IgniteCache<Object, Object> cache, String key) {
		Object key1 = cache.get(key);

		if (key1 != null) {
			System.out.println("##################SUCCESS##########################");
			System.out.println("Value is : {}" + key1.toString());
		}
	}
}

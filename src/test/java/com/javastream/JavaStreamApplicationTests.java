package com.javastream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastream.domain.dto.Purchase;
import com.javastream.domain.dto.Transaction;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
class JavaStreamApplicationTests {

	@Autowired
	ResourceLoader resourceLoader;
	private ObjectMapper objectMapper = new ObjectMapper();
	private final List<Transaction> transactions = new ArrayList<>();

	@BeforeEach
	void setup() {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		Resource resource = resolver.getResource("transactions.ndjson");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					Transaction transaction = objectMapper.readValue(line, Transaction.class);
					transactions.add(transaction);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	LocalDate convertMilToDate(Long date) {
		Instant instant = Instant.ofEpochMilli((Long) date);
		return LocalDate.ofInstant(instant, java.util.TimeZone.getDefault().toZoneId());
	}

	@Test
	@SneakyThrows
	void findUserWithHighestTransactionAmount_Test() {
		DecimalFormat formatter = new DecimalFormat("#,###.##");
		formatter.setGroupingSize(3); // Set comma for every 3 digits
		formatter.setMinimumFractionDigits(2);

		var userTransaction = transactions.stream()
			.collect(Collectors.groupingBy(Transaction::getUser,
				Collectors.collectingAndThen(
					Collectors.summingDouble(
						t -> t.getPurchases().stream().mapToDouble(
							p -> p.getPrice().doubleValue() * p.getCount()
						).sum()),
					BigDecimal::new)))
			.entrySet().stream()
			.sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
			.collect(Collectors.toMap(
					Map.Entry::getKey,
					entry -> formatter.format(entry.getValue()),
					(oldValue, newValue) -> oldValue, // Merge function for duplicate keys (not necessary in this case)
					LinkedHashMap::new)); // Preserve ordering of the sorted map

			System.out.println(objectMapper.writeValueAsString(userTransaction));
	}

	@Test
	@SneakyThrows
	void findCategoryWithMostItemsSold_Test() {
		var categoryTotalCounts = transactions.stream()
			.flatMap(transaction -> transaction.getPurchases().stream())
				.collect(Collectors.groupingBy(Purchase::getCategory,
					Collectors.summingInt(
						Purchase::getCount
					)))
					.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(oldValue, newValue) -> oldValue, // Merge function for duplicate keys (not necessary in this case)
					LinkedHashMap::new // Preserve ordering of the sorted map
				));

		System.out.println(objectMapper.writeValueAsString(categoryTotalCounts));
	}

	@Test
	@SneakyThrows
	void findShippingHighestUsage_Test() {
		Map<String, Long> shippingCountMap = transactions.stream()
				.collect(Collectors.groupingBy(Transaction::getShipping, Collectors.counting()))
				.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

		System.out.println(objectMapper.writeValueAsString(shippingCountMap));
	}

	@Test
	@SneakyThrows
	void findTransactionBySpesificMonth_Test() {
		int targetMonth = 1;
		var transactionByMonth = transactions.stream()
				.filter(transaction -> convertMilToDate(transaction.getTransactionDate()).getMonth().getValue() == targetMonth)
				.map(transaction -> {
					Map<String, Object> filteredData = new HashMap<>();
					filteredData.put("transaction_id", transaction.getTransactionId());
					filteredData.put("transaction_date", convertMilToDate(transaction.getTransactionDate()).toString());
					return filteredData;
				})
				.collect(Collectors.toList());

		System.out.println(objectMapper.writeValueAsString(transactionByMonth));
	}

	@Test
	@SneakyThrows
	void findMonthMostTransactions_Test(){
		var monthHighestTransaction = transactions.stream()
				.mapToLong(transaction -> {
					return convertMilToDate(transaction.getTransactionDate()).getMonthValue(); // Extract month (1-12)
				})
				.boxed() // Convert back to Stream<Integer> for grouping
				.collect(Collectors.groupingBy(month -> month, Collectors.counting()))
				.entrySet().stream()
				.sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
				.collect(Collectors.toMap(
						entry -> Month.of(entry.getKey().intValue()).toString(),  // Format month name
						Map.Entry::getValue,
						(v1, v2) -> v1,
						LinkedHashMap::new
				));

		System.out.println(objectMapper.writeValueAsString(monthHighestTransaction));
	}
}

# Databricks notebook source
from pyspark.sql import functions as F

source_date = dbutils.widgets.get("source_date")
bronze = spark.read.table("portfolio.bronze.sales").where(F.col("source_date") == source_date)
silver = bronze.dropDuplicates(["order_id"]).filter(F.col("amount").isNotNull())
silver.write.mode("overwrite").saveAsTable("portfolio.silver.sales")
gold = silver.groupBy("country").agg(F.sum("amount").alias("revenue"), F.count("order_id").alias("orders"))
gold.write.mode("overwrite").saveAsTable("portfolio.gold.sales_daily")

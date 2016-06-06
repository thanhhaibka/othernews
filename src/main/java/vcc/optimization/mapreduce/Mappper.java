package vcc.optimization.mapreduce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Mappper extends Mapper<LongWritable, Text, Text, Text> {
	@Override
	protected void setup(Context context) {
		Configuration conf = context.getConfiguration();
		String[] array = conf.getStrings("listNews");
		ArrayList<String> listNews = new ArrayList<String>(array.length);
		Collections.addAll(listNews, array);
	}

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		String[] array = conf.getStrings("listNews");
		for (String str : array) {
			context.write(new Text(), new Text(str));
		}
	}
}

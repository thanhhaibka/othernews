package vcc.optimization.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import vcc.optimization.othernews.ConnectMySQL;

public class User extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		ConnectMySQL.getInstance();
		ToolRunner.run(new User(), args);
		System.exit(1);
	}

	public int run(String[] arg0) throws Exception {
		Configuration conf = new Configuration();
		String[] listNews = new String[ConnectMySQL.getInstance().getList().size()];
		ConnectMySQL.getInstance().getList().toArray(listNews);
		conf.setStrings("listNews", listNews);
		conf.addResource(new Path("config/core-site.xml"));
		conf.addResource(new Path("config/hbase-site.xml"));
		conf.addResource(new Path("config/hdfs-site.xml"));
		conf.addResource(new Path("config/mapred-site.xml"));
		conf.addResource(new Path("config/yarn-site.xml"));
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "ExportData Training");
		job.setJarByClass(User.class);
		
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		
		
		job.setMapperClass(Mappper.class);

		FileSystem fileSystem = FileSystem.get(conf);

		// Output
		if (fileSystem.exists(new Path("/user/chienpq/outTrainData"))) {
			// Delete file
			fileSystem.delete(new Path("/user/chienpq/outTrainData"), true);
		}
		FileOutputFormat.setOutputPath(job, new Path("/user/chienpq/outTrainData"));
		return job.waitForCompletion(true) ? 0 : 1;

	}
}

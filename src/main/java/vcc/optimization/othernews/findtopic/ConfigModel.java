package vcc.optimization.othernews.findtopic;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

/**
 * @author datluyen
 *
 */
public class ConfigModel {

	private  String modelDir = "mallet-model";
	private final  String ldaModelSave = "mallet-ldamodel.gz";
	private final  String topicKeyWord = "topicKeysFile.dat";
	private final  String diagnostics = "diagnosticsFile.dat";
	private final  String inferenceFile = "inferencefile.model";
	private final  String previousInstanceListFile = "input.mallet";
	
	public ConfigModel(){
		
	}
	
	public ConfigModel(String s){
		modelDir = s;
	}
	
	public String getLdaModel() {
		return modelDir + "/" + ldaModelSave;
	}

	public String getTopicKeyWordFile() {
		return modelDir + "/" + topicKeyWord;
	}
	
	public String getDiagnostics(){
		return modelDir + "/" + diagnostics;
	}
	
	
	public String getInferenceFile(){
		return modelDir + "/" + inferenceFile;
	}

	public  String getPreviousinstancelistfile() {
		return modelDir + "/" +  previousInstanceListFile;
	}

	public static ConfigModel configModel = null;
	
	public static ConfigModel getInstance(){
		if (configModel == null){
			configModel = new ConfigModel();
		}
		
		return configModel;
	}
	
	public static ConfigModel getInstance(String s){
		if (configModel == null){
			configModel = new ConfigModel(s);
		}
		
		return configModel;
	}
}

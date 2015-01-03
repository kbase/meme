package us.kbase.meme;

import java.util.regex.Pattern;

import org.apache.commons.cli.*;

public class MemeServerInvoker {

	/**
	 * @param args
	 */
	
	Options options = new Options();
	final static Pattern p = Pattern.compile("^'(.*)'$");

	@SuppressWarnings("static-access")
	public MemeServerInvoker() {

		options.addOption( OptionBuilder.withLongOpt( "help" )
                .withDescription( "print this message" )
                .withArgName("help")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "test" )
                .withDescription( "for testing only" )
                .withArgName("test")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "method" )
                .withDescription( "available methods: find_motifs_with_meme_job_from_ws | compare_motifs_with_tomtom_by_collection_from_ws | compare_motifs_with_tomtom_job_by_collection_from_ws | find_sites_with_mast_by_collection_from_ws | find_sites_with_mast_job_by_collection_from_ws | get_pspm_collection_from_meme_result_job_from_ws" )
                .hasArg(true)
                .withArgName("NAME")
                .create() );
		
		options.addOption( OptionBuilder.withLongOpt( "job" )
                .withDescription( "job ID" )
                .hasArg(true)
                .withArgName("job")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "ws" )
                .withDescription( "workspace ID" )
                .hasArg(true)
                .withArgName("workspace_id")
                .create() );
		
		options.addOption( OptionBuilder.withLongOpt( "query" )
                .withDescription( "query ID: sequence set ID for MEME, PSPM (PSPM collection) ID for TOMTOM and MAST" )
                .hasArg(true)
                .withArgName("query_id")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "target" )
                .withDescription( "target ID: PSPM (PSPM collection) ID for TOMTOM or sequence set ID for MAST" )
                .hasArg(true)
                .withArgName("target_id")
                .create() );
		
		options.addOption( OptionBuilder.withLongOpt( "pspm" )
                .withDescription( "PSPM ID in collection: TOMTOM or MAST run against a single PSPM from a collection" )
                .hasArg(true)
                .withArgName("pspm_id")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "mod" )
                .withDescription( "mode of distribution for MEME: oops | zops | anr" )
                .hasArg(true)
                .withArgName("mod")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "nmotifs" )
                .withDescription( "MEME option: number of motifs" )
                .hasArg(true)
                .withArgName("nmotifs")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "minw" )
                .withDescription( "MEME option: minimum motif width" )
                .hasArg(true)
                .withArgName("minw")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "maxw" )
                .withDescription( "MEME option: maximum motif width" )
                .hasArg(true)
                .withArgName("maxw")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "nsites" )
                .withDescription( "MEME option: number of sites for each motif" )
                .hasArg(true)
                .withArgName("nsites")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "minsites" )
                .withDescription( "MEME option: minimum number of sites for each motif" )
                .hasArg(true)
                .withArgName("minsites")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "maxsites" )
                .withDescription( "MEME option: maximum number of sites for each motif" )
                .hasArg(true)
                .withArgName("maxsites")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "pal" )
                .withDescription( "MEME option: force palindromes: 0 | 1" )
                .hasArg(true)
                .withArgName("pal")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "revcomp" )
                .withDescription( "MEME option: allow sites on + or - DNA strands. Possible values 0 or 1. Default(0): look for DNA motifs only on the strand given in the training set" )
                .hasArg(true)
                .withArgName("revcomp")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "thresh" )
                .withDescription( "TOMTOM and MAST option: only report matches with significance values ≤ thresh parameter value. " )
                .hasArg()
                .withArgName("thresh")
                .create() );
		
		options.addOption( OptionBuilder.withLongOpt( "evalue" )
                .withDescription( "TOMTOM option: use the E-value of the match as the significance threshold: 0 | 1" )
                .hasArg(true)
                .withArgName("evalue")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "dist" )
                .withDescription( "TOMTOM option: Motif distance calculation algorithm: allr | ed | kullback | pearson | sandelin" )
                .hasArg(true)
                .withArgName("dist")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "internal" )
                .withDescription( "TOMTOM option: this parameter forces the shorter motif to be completely contained in the longer motif. Possible values: 0 | 1 " )
                .hasArg(true)
                .withArgName("internal")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "min_overlap" )
                .withDescription( "TOMTOM option: only report motif matches that overlap by min overlap positions or more" )
                .hasArg(true)
                .withArgName("min_overlap")
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "token" )
                .withDescription( "Authorization token" )
                .hasArg(true)
                .withArgName("token")
                .create() );

	}

	private String runMeme (CommandLine line) throws Exception{

		MemeRunParameters params = new MemeRunParameters();		    			
		String returnVal = null;
		
		params.setSourceRef(cleanUpArgument(line.getOptionValue("query")));
		
		if ( line.hasOption("mod")){
			params.setMod(cleanUpArgument(line.getOptionValue("mod")));
		}
		else {
			System.err.println( "Required MEME mod parameter missed");
			System.exit(1);
		}
		
		if ( line.hasOption("nmotifs")){
			params.setNmotifs(Long.parseLong(cleanUpArgument(line.getOptionValue("nmotifs"))));
		}
		else {
			params.setNmotifs(0L);
		}
		
		if ( line.hasOption("minw")){
			params.setMinw(Long.parseLong(cleanUpArgument(line.getOptionValue("minw"))));
		}
		else {
			params.setMinw(0L);
		}
		
		if ( line.hasOption("maxw")){
			params.setMaxw(Long.parseLong(cleanUpArgument(line.getOptionValue("maxw"))));
		}
		else {
			params.setMaxw(0L);
		}

		if ( line.hasOption("nsites")){
			params.setNsites(Long.parseLong(cleanUpArgument(line.getOptionValue("nsites"))));
		}
		else {
			params.setNsites(0L);
		}

		if ( line.hasOption("minsites")){
			params.setMinsites(Long.parseLong(cleanUpArgument(line.getOptionValue("minsites"))));
		}
		else {
			params.setMinsites(0L);
		}

		if ( line.hasOption("maxsites")){
			params.setMaxsites(Long.parseLong(cleanUpArgument(line.getOptionValue("maxsites"))));
		}
		else {
			params.setMaxsites(0L);
		}

		if ( line.hasOption("pal")){
			params.setPal(Long.parseLong(cleanUpArgument(line.getOptionValue("pal"))));
		} 
		else {
			params.setPal(0L);
		}

		if ( line.hasOption("revcomp")){
			params.setRevcomp(Long.parseLong(cleanUpArgument(line.getOptionValue("revcomp"))));
		} 
		else {
			params.setRevcomp(0L);
		}

		returnVal = MemeServerImpl.findMotifsWithMemeJobFromWs(cleanUpArgument(line.getOptionValue("ws")), 
							params,
							cleanUpArgument(line.getOptionValue("job")),
							cleanUpArgument(line.getOptionValue("token")));
		
		return returnVal;
		
	}
	
/*	private String runTomtomWithMatrix (CommandLine line) throws Exception {

		TomtomRunParameters params = new TomtomRunParameters();
		String returnVal = null;

		if ( line.hasOption("thresh")){
			params.setThresh(Double.parseDouble(line.getOptionValue("thresh")));
		}
		else {
			params.setThresh(0.0);
		}

		if ( line.hasOption("dist")){
			params.setDist(line.getOptionValue("dist"));
		}
		else {
			System.err.println( "Required TOMTOM dist parameter missed");
			System.exit(1);
		}

		if ( line.hasOption("min_overlap")){
			params.setMinOverlap(Long.parseLong(line.getOptionValue("min_overlap")));
		}
		else {
			params.setMinOverlap(0L);
		}

		if ( line.hasOption("evalue")){
			params.setEvalue(Long.parseLong(line.getOptionValue("evalue")));
		} 
		else {
			params.setEvalue(0L);
		}
		
		if ( line.hasOption("internal")){
			params.setInternal(Long.parseLong(line.getOptionValue("internal")));
		} 
		else {
			params.setInternal(0L);
		}
		
		if (line.hasOption("target")) {
			returnVal = MemeServerImpl.compareMotifsWithTomtomJobFromWs(line.getOptionValue("ws"), 
								line.getOptionValue("query"), 
								line.getOptionValue("target"), 
								params, 
								line.getOptionValue("job"),
								line.getOptionValue("token"));
		}
		else {
			System.err.println( "Target ID required");
			System.exit(1);
		}
		return returnVal;

	}
*/

	private String runTomtomWithCollection (CommandLine line) throws Exception {

		TomtomRunParameters params = new TomtomRunParameters();
		String returnVal = null;
		
		if ( line.hasOption("thresh")){
			params.setThresh(Double.parseDouble(cleanUpArgument(line.getOptionValue("thresh"))));
		}
		else {
			params.setThresh(0.0);
		}

		if ( line.hasOption("dist")){
			params.setDist(cleanUpArgument(line.getOptionValue("dist")));
		}
		else {
			System.err.println( "Required TOMTOM dist parameter missed");
			System.exit(1);
		}

		if ( line.hasOption("min_overlap")){
			params.setMinOverlap(Long.parseLong(cleanUpArgument(line.getOptionValue("min_overlap"))));
		}
		else {
			params.setMinOverlap(0L);
		}

		if ( line.hasOption("evalue")){
			params.setEvalue(Long.parseLong(cleanUpArgument(line.getOptionValue("evalue"))));
		} 
		else {
			params.setEvalue(0L);
		}

		if ( line.hasOption("internal")){
			params.setInternal(Long.parseLong(cleanUpArgument(line.getOptionValue("internal"))));
		} 
		else {
			params.setInternal(0L);
		}

		if ( line.hasOption("pspm")){
			if (!cleanUpArgument(line.getOptionValue("pspm")).equals("null"))
				params.setPspmId(cleanUpArgument(line.getOptionValue("pspm")));
		}
		
		if (line.hasOption("target")) {
			params.setQueryRef(cleanUpArgument(line.getOptionValue("query")));
			params.setTargetRef(cleanUpArgument(line.getOptionValue("target")));
			returnVal = MemeServerImpl.compareMotifsWithTomtomJobByCollectionFromWs(cleanUpArgument(line.getOptionValue("ws")), 
								params, 
								cleanUpArgument(line.getOptionValue("job")),
								cleanUpArgument(line.getOptionValue("token")));
		
		}
		else {
			System.err.println( "Target ID required");
			System.exit(1);
		}
		
		return returnVal;
	}


/*	private String runMastWithMatrix (CommandLine line) throws Exception {
		Double mt = 0.0;
		String returnVal = null;

		MastRunParameters params = new MastRunParameters();
		
		if (line.hasOption("thresh")){
			params.setMt(Double.parseDouble(line.getOptionValue("thresh")));
		}
		else {
			params.setMt(Double.valueOf("0.0"));
		}
		
		

		if (line.hasOption("target")) {
			returnVal = MemeServerImpl.findSitesWithMastJobFromWs(line.getOptionValue("ws"), 
								line.getOptionValue("query"), 
								line.getOptionValue("target"), 
								mt, 
								line.getOptionValue("job"),
								line.getOptionValue("token"));
		}
		else {
			System.err.println( "Target ID required");
			System.exit(1);
		}
		
		return returnVal;
	}
*/

	private String runMastWithCollection (CommandLine line) throws Exception {
		String returnVal = null;
		MastRunParameters params = new MastRunParameters();

		if ( line.hasOption("pspm")){
			if (!cleanUpArgument(line.getOptionValue("pspm")).equals("null"))
				params.setPspmId(cleanUpArgument(line.getOptionValue("pspm")));
		}

		if (line.hasOption("thresh")){
			params.setMt(Double.parseDouble(cleanUpArgument(line.getOptionValue("thresh"))));
		}
		else {
			params.setMt(Double.valueOf("0.0"));
		}
		
		if (line.hasOption("target")) {
			params.setQueryRef(cleanUpArgument(line.getOptionValue("query")));
			params.setTargetRef(cleanUpArgument(line.getOptionValue("target")));
			returnVal = MemeServerImpl.findSitesWithMastJobByCollectionFromWs(cleanUpArgument(line.getOptionValue("ws")),
								params,
								cleanUpArgument(line.getOptionValue("job")),
								cleanUpArgument(line.getOptionValue("token")));
		}
		else {
			System.err.println( "Target ID required");
			System.exit(1);
		}
		return returnVal;
	}

	private String generateCollection (CommandLine line) throws Exception {
		String returnVal = null;
		returnVal = MemeServerImpl.getPspmCollectionFromMemeJobResultFromWs(cleanUpArgument(line.getOptionValue("ws")), 
				cleanUpArgument(line.getOptionValue("query")), 
				cleanUpArgument(line.getOptionValue("job")),
				cleanUpArgument(line.getOptionValue("token")));		
		return returnVal;
	}

	public void run (String[] args) throws Exception{

		String serverMethod = "";
		CommandLineParser parser = new GnuParser();
		String returnVal = "";

		try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args);
		    if( line.hasOption( "help" ) ) {
		    	// automatically generate the help statement
		    	HelpFormatter formatter = new HelpFormatter();
		    	formatter.printHelp( "java -jar /kb/deployment/meme/meme.jar [parameters]", options );

		    }
		    else if( line.hasOption( "test" ) ) {
		    	// return nothing and exit
		    	System.exit(0);
		    }
		    else {
		    	if ( validateInput(line)){
		    		serverMethod = line.getOptionValue( "method" );

		    		if (serverMethod.equalsIgnoreCase("find_motifs_with_meme_job_from_ws")){
		    			returnVal = runMeme(line);
		    		}
/*		    		else if (serverMethod.equalsIgnoreCase("compare_motifs_with_tomtom_job_from_ws")){
		    			returnVal = runTomtomWithMatrix(line);
		    		}
*/
		    		else if (serverMethod.equalsIgnoreCase("compare_motifs_with_tomtom_job_by_collection_from_ws")){
		    			returnVal = runTomtomWithCollection(line);
		    		}
/*		    		else if (serverMethod.equalsIgnoreCase("find_sites_with_mast_job_from_ws")){
		    			returnVal = runMastWithMatrix(line);
		    		}
*/
		    		else if (serverMethod.equalsIgnoreCase("find_sites_with_mast_job_by_collection_from_ws")){
		    			returnVal = runMastWithCollection(line);
		    		}
		    		else if (serverMethod.equalsIgnoreCase("get_pspm_collection_from_meme_result_job_from_ws")){
		    			returnVal = generateCollection(line);
		    		}
		    		else {
		    			System.err.println( "Unknown method: " + serverMethod + "\n");
				    	HelpFormatter formatter = new HelpFormatter();
				    	formatter.printHelp( "java -jar /kb/deployment/meme/meme.jar [parameters]", options );
		    			System.exit(1);
		    		}
		    		// do something with resulting value
	    			returnResult(returnVal);
 
		    	}
		    	else {
			    	HelpFormatter formatter = new HelpFormatter();
			    	formatter.printHelp( "java -jar /kb/deployment/meme/meme.jar [parameters]", options );
		    		System.exit(1);
		    	}
		    }
	        
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }

	}

	
	private static boolean validateInput (CommandLine line) {
		boolean returnVal = false;
		if (line.hasOption("method")){

			if (line.hasOption("ws")){
				
				if (line.hasOption("token")){

					if (line.hasOption("query")){

						if (line.hasOption("job")){

							returnVal = true;
						}
						else {
							System.err.println( "Job ID required");
						}
						
					} 
					else {
						System.err.println( "Query ID required");
					}

				}
				else {
					System.err.println( "Authorization required");
				}
				
			}
			else {
				System.err.println( "Workspace ID required");
			}

		}
		else {
			System.err.println( "Method required");
		}
		return returnVal;
	}

	public void returnResult (String resultId) {
		System.out.println(resultId);
	}

	protected static String cleanUpArgument (String argument){
		if (argument.matches(p.pattern())){
			argument = argument.replaceFirst(p.pattern(), "$1");
		}
		return argument;
	}

	public static void main(String[] args) throws Exception {
		
		MemeServerImpl.startUp();
		MemeServerInvoker tug = new MemeServerInvoker();
		tug.run(args);

	    
	}
	

}

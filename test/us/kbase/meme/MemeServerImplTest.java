package us.kbase.meme;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonClientCaller;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.kbasesequences.SequenceSet;
import us.kbase.kbasesequences.Sequence;
import us.kbase.userandjobstate.InitProgress;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.workspace.ObjectIdentity;
import us.kbase.workspace.WorkspaceClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class MemeServerImplTest {
	
	private final String USER_NAME = "";
	private final String PASSWORD = "";
	private final String TEST_WORKSPACE = "AKtest";

	private SequenceSet testSequenceSet = new SequenceSet();
	private String testSequenceSetId = "mod_desulfovibrio";//"Halobacterium_sp_NRC-1_Idr2_regulon";
	private String testCollectionId = "kb|memepspmcollection.57";//"kb|memepspmcollection.41";	
	private String testMemeRunResultId = "kb|memerunresult.187";
	private String testMemePspmId = "kb|memepspm.115";	
	private String fakeJobId = "12345.fasta";

	private String inputSequenceSet = new String();
	//private MemeRunResult memeRunResult = new MemeRunResult();
	private final String JOB_SERVICE = "https://kbase.us/services/userandjobstate";
	private final String WS_SERVICE = "https://kbase.us/services/ws";
	private static AuthToken token = null;
	
	@Before
	public void setUp() throws Exception {
		try {
			token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		} catch (AuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testSequenceSetId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		testSequenceSet = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(SequenceSet.class);

    	for(Sequence sequence:testSequenceSet.getSequences()){
    		inputSequenceSet += ">"+sequence.getSequenceId()+"\n";
    		inputSequenceSet += MemeServerImpl.formatSequence(sequence.getSequence())+"\n";
    	}
	}

	@After
	public void tearDown() throws Exception {
		testSequenceSet = null;
		Runtime.getRuntime().exec("rm " + fakeJobId);
	}

	
	@Test
	public void testGenerateMemeInput() throws IOException {
		MemeServerImpl.generateFastaFile(fakeJobId, testSequenceSet);
		String result = new String();
		try {
			BufferedReader textreader = new BufferedReader(new FileReader(fakeJobId));
			String line = new String();
			while ((line = textreader.readLine())!=null){
				result += line+"\n";
			}
			textreader.close();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			
		}
		assertEquals(inputSequenceSet,result);
	}

	@Test
	public void testGenerateMemeCommandLine() throws UnsupportedEncodingException {
//		String modMeme = new String("anr".getBytes("UTF-8"), "ISO-8859-1");
		String modMeme = "anr";
		String result = MemeServerImpl.generateMemeCommandLine(fakeJobId, modMeme, 5L, 8L, 22L, 2L, 2L, 3L, 0L, 1L);
		assertEquals("meme "+fakeJobId+" -mod anr -nmotifs 5 -minw 8 -maxw 22 -nsites 2 -minsites 2 -maxsites 3 -revcomp -dna -text -nostatus",result);
	}

	@Test
	public void testRunMeme() {
		String testFileName = "test/testoutput.txt";
		MemeServerImpl.executeCommand("meme", testFileName);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(testFileName));
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				Runtime.getRuntime().exec("rm " + testFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertEquals("                    MEME — Multiple EM for Motif Elicitation", line);
	}

	@Test
	public void testWhichR() {
		String testFileName = "test/javaoutput.txt";
		MemeServerImpl.executeCommand("which R", testFileName);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(testFileName));
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				Runtime.getRuntime().exec("rm " + testFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertEquals("                    MEME — Multiple EM for Motif Elicitation", line);
	}

	
	@Test
	public void testProcessMotifDataLine() {
		MemeMotif result = new MemeMotif();
		MemeServerImpl.processMotifDataLine(result, "MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003");
		assertEquals("MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003",result.getDescription());
		assertEquals(Long.valueOf("24"),result.getWidth());
		assertEquals(Double.valueOf("90"),result.getLlr());
		assertEquals(Double.valueOf("2300"),result.getEvalue());
	}

	@Test
	public void testProcessSampleLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processSampleLine(result, "sample: seed=            0    seqfrac=         1");
		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
	}

	@Test
	public void testProcessStrandsLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processStrandsLine(result, "strands: +");
		assertEquals("+",result.getStrands());
	}

	@Test
	public void testProcessDataNLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processDataNLine(result, "data:   n=            1500    N=               6");
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
	}

	@Test
	public void testProcessDistanceLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processDistanceLine(result, "        distance=    1e-05");
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
	}

	@Test
	public void testProcessEmLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processEmLine(result, "em:     prior=   dirichlet    b=            0.01    maxiter=        50");
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
	}

	@Test
	public void testProcessGlobalsLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processGlobalsLine(result, "global: substring=     yes    branching=      no    wbranch=        no");
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
	}

	@Test
	public void testProcessThetaLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processThetaLine(result, "theta:  prob=            1    spmap=         uni    spfuzz=        0.5");
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
	}

	@Test
	public void testProcessNsitesLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processNsitesLine(result, "nsites: minsites=        6    maxsites=        6    wnsites=       0.8");
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
	}

	@Test
	public void testProcessWgLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processWgLine(result, "width:  wg=             11    ws=              1    endgaps=       yes");
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
	}

	@Test
	public void testProcessMinwLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processMinwLine(result, "width:  minw=           14    maxw=           24    minic=        0.00");
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
	}

	@Test
	public void testProcessOFLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processOFLine(result, "object function=  E-value of product of p-values");
		assertEquals("E-value of product of p-values",result.getObjectFunction());
	}

	@Test
	public void testProcessModLine() {
		MemeRunResult result = new MemeRunResult();
		MemeServerImpl.processModLine(result, "model:  mod=          oops    nmotifs=         2    evt=           inf");
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
	}

	@Test
	public void testGenerateSiteMeme() {
		MemeSite result = new MemeSite();
		result = MemeServerImpl.generateMemeSite("393587                      134  1.52e-10 ACTGGTTTTG TCACGATTTTCAGGACATTCGTGA CCGCGTTGGC");
		assertEquals("393587",result.getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getPvalue());
		assertEquals("ACTGGTTTTG",result.getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getSequence());
		assertEquals("CCGCGTTGGC",result.getRightFlank());
	}

	@Test
	public void testGenerateMotifMeme() throws Exception {
		List<MemeMotif> motifs = new ArrayList<MemeMotif>();
		List<String> sites = new ArrayList<String>();
		sites.add("393587                      134  1.52e-10 ACTGGTTTTG TCACGATTTTCAGGACATTCGTGA CCGCGTTGGC");
		sites.add("209110                      122  6.21e-10 GTGAATGCGG TCACGTTCGTCACCATGAGGGTGA CCGGGTTGCC");
		sites.add("209112                      152  9.16e-10 GGCAACCCGG TCACCCTCATGGTGACGAACGTGA CCGCATTCAC");
		sites.add("7532041                      75  4.75e-09 CGGCTACCGG TCACGCTTTTCCGCGCTACCGTGA CCGGCTTGAG");
		sites.add("8501762                     181  8.64e-07 CTGCCCCGTG CCACGGTCACCAAGACGAAAGTTT TCGTGCCTCT");
		sites.add("209114                      215  2.84e-05 GGGGCGTACG CCACGCATCCACATGACACCATAA CCGGGAAGAC");
		MemeServerImpl.generateMemeMotif("MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003", "cumulativeOutput", sites, motifs);
		MemeMotif result = motifs.get(0);
		assertEquals("MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003",result.getDescription());
		assertEquals(Long.valueOf("24"),result.getWidth());
		assertEquals(6,result.getSites().size());
		assertEquals(Double.valueOf("90"),result.getLlr());
		assertEquals(Double.valueOf("2300"),result.getEvalue());
		assertEquals("393587",result.getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getSites().get(0).getRightFlank());
	}

	@Test
	public void testParseMemeOutput() throws Exception {
		String testOutputFile = "test/mem_12345.txt";

		MemeRunResult result = MemeServerImpl.parseMemeOutput(testOutputFile);

		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
		assertEquals("+",result.getStrands());
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
		assertEquals("E-value of product of p-values",result.getObjectFunction());
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
		assertEquals("MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003",result.getMotifs().get(0).getDescription());
		assertEquals(Long.valueOf("24"),result.getMotifs().get(0).getWidth());
		assertEquals(6,result.getMotifs().get(0).getSites().size());
		assertEquals(Double.valueOf("90"),result.getMotifs().get(0).getLlr());
		assertEquals(Double.valueOf("2300"),result.getMotifs().get(0).getEvalue());
		assertEquals("393587",result.getMotifs().get(0).getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getMotifs().get(0).getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getMotifs().get(0).getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getMotifs().get(0).getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getMotifs().get(0).getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getMotifs().get(0).getSites().get(0).getRightFlank());
	}

	@Test
	public void testSearchMotifsFromSequencesWithMeme() throws Exception {
//		String modMeme = new String("oops".getBytes("UTF-8"), "ISO-8859-1");
		MemeRunParameters params = new MemeRunParameters();
		params.setMod("oops");
		params.setNmotifs(2L);
		params.setMinw(14L);
		params.setMaxw(24L);
		params.setNsites(0L);
		params.setMinsites(0L);
		params.setMaxsites(0L);
		params.setPal(1L);
		params.setRevcomp(0L);
		params.setSourceId(testSequenceSetId);
		params.setSourceRef(TEST_WORKSPACE + "/" + testSequenceSetId);

		
		MemeRunResult result = MemeServerImpl.findMotifsWithMeme(testSequenceSet, params, null, null);

		
		//displayCollection(result);

		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
		assertEquals("+",result.getStrands());
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
		assertEquals("E-value of product of p-values",result.getObjectFunction());
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
		assertEquals("MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003",result.getMotifs().get(0).getDescription());
		assertEquals(Long.valueOf("24"),result.getMotifs().get(0).getWidth());
		assertEquals(6,result.getMotifs().get(0).getSites().size());
		assertEquals(Double.valueOf("90"),result.getMotifs().get(0).getLlr());
		assertEquals(Double.valueOf("2300"),result.getMotifs().get(0).getEvalue());
		assertEquals("kb|sequence.43",result.getMotifs().get(0).getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getMotifs().get(0).getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getMotifs().get(0).getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getMotifs().get(0).getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getMotifs().get(0).getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getMotifs().get(0).getSites().get(0).getRightFlank());
	}

	@Test
	public void testSearchMotifsFromSequencesWithMemeJob() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		String jobId = jobClient.createJob();
		System.out.println(jobId);
		assertNotNull(jobId);

		MemeRunParameters params = new MemeRunParameters();
		params.setSourceRef(TEST_WORKSPACE + "/" + testSequenceSetId);
		params.setMod("oops");
		params.setNmotifs(2L);
		params.setMinw(14L);
		params.setMaxw(24L);
		params.setNsites(0L);
		params.setMinsites(0L);
		params.setMaxsites(0L);
		params.setPal(1L);
		params.setRevcomp(0L);
	
		String resultId = MemeServerImpl.findMotifsWithMemeJobFromWs (TEST_WORKSPACE, params, jobId, token.toString());
		
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemeRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemeRunResult.class);

		displayCollection(result);

		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
		assertEquals("+",result.getStrands());
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
		assertEquals("E-value of product of p-values",result.getObjectFunction());
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
		assertEquals("MOTIF  1	width =   24   sites =   6   llr = 90   E-value = 2.3e+003",result.getMotifs().get(0).getDescription());
		assertEquals(Long.valueOf("24"),result.getMotifs().get(0).getWidth());
		assertEquals(6,result.getMotifs().get(0).getSites().size());
		assertEquals(Double.valueOf("90"),result.getMotifs().get(0).getLlr());
		assertEquals(Double.valueOf("2300"),result.getMotifs().get(0).getEvalue());
		assertEquals("kb|sequence.43",result.getMotifs().get(0).getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getMotifs().get(0).getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getMotifs().get(0).getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getMotifs().get(0).getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getMotifs().get(0).getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getMotifs().get(0).getSites().get(0).getRightFlank());
	}

	@Test
	public void testGenerateTomtomCommandLine() {
		TomtomRunParameters params = new TomtomRunParameters().withDist("allr").withInternal(1L).withThresh(0.5D).withEvalue(1L).withMinOverlap(15L).withQueryRef("test1").withTargetRef("test2");
		String result = MemeServerImpl.generateTomtomCommandLine("1.meme", "2.meme", params);
		assertEquals("tomtom -thresh 0.5 -evalue -dist allr -internal -min-overlap 15 -text 1.meme 2.meme",result);
		
	}

	@Test
	public void testRunTomtom() {
		String testFileName = "test/tomtomoutput.txt"; 
		MemeServerImpl.executeCommand("tomtom -dist pearson -text test/mem_12345.txt test/mem_12345.txt", testFileName);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(testFileName));
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				Runtime.getRuntime().exec("rm " + testFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertEquals("#Query ID	Target ID	Optimal offset	p-value	E-value	q-value	Overlap	Query consensus	Target consensus	Orientation",line);
	}
	
	@Test
	public void testGenerateHitTomtom() {
		String hit = "1	1	0	3.89353e-37	7.78706e-37	7.78706e-37	24	TCACGCTCGTCATGACGAGCGTGA	TCACGCTCGTCATGACGAGCGTGA	+";
		TomtomHit result = MemeServerImpl.generateHitTomtom(hit);
		assertEquals("1",result.getQueryPspmId());
		assertEquals("1",result.getTargetPspmId());
		assertEquals(Long.valueOf("0"),result.getOptimalOffset());
		assertEquals(Double.valueOf("3.89353E-37"),result.getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getEvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getQvalue());
		assertEquals(Long.valueOf("24"),result.getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getTargetConsensus());
		assertEquals("+",result.getStrand());
	}

	@Test
	public void testCompareMotifsWithTomtom() throws Exception {
		
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testCollectionId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection memePspmCollection = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);
		
		TomtomRunParameters paramsTomtom = new TomtomRunParameters();
		paramsTomtom.setDist("pearson");
		paramsTomtom.setThresh(0.00);
		paramsTomtom.setEvalue(0L);
		paramsTomtom.setInternal(0L);
		paramsTomtom.setMinOverlap(0L);
		paramsTomtom.setQueryRef("");
		paramsTomtom.setTargetRef("");

		TomtomRunResult result = MemeServerImpl.compareMotifsWithTomtomByCollection(memePspmCollection, memePspmCollection, paramsTomtom, null, null);
		
		assertNotNull(result.getHits().get(0).getTargetPspmId());
		assertEquals(result.getHits().get(0).getTargetPspmId(),result.getHits().get(0).getQueryPspmId());
		assertEquals(Long.valueOf("0"),result.getHits().get(0).getOptimalOffset());
		assertEquals(Double.valueOf("3.89353E-37"),result.getHits().get(0).getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getEvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getQvalue());
		assertEquals(Long.valueOf("24"),result.getHits().get(0).getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getTargetConsensus());
		assertEquals("+",result.getHits().get(0).getStrand());
	}

	@Test
	public void testCompareSingleMotifWithTomtom() throws Exception {
		
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testCollectionId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection memePspmCollection = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);

		TomtomRunParameters paramsTomtom = new TomtomRunParameters();
		paramsTomtom.setDist("pearson");
		paramsTomtom.setThresh(0.00);
		paramsTomtom.setEvalue(0L);
		paramsTomtom.setInternal(0L);
		paramsTomtom.setMinOverlap(0L);
		paramsTomtom.setPspmId(testMemePspmId);
		paramsTomtom.setQueryRef("");
		paramsTomtom.setTargetRef("");

		TomtomRunResult result = MemeServerImpl.compareMotifsWithTomtomByCollection(memePspmCollection, memePspmCollection, paramsTomtom, null, null);
		
		assertNotNull(result.getHits().get(0).getTargetPspmId());
		assertEquals(result.getHits().get(0).getTargetPspmId(),result.getHits().get(0).getQueryPspmId());
		assertEquals(Long.valueOf("0"),result.getHits().get(0).getOptimalOffset());
		assertEquals(Double.valueOf("3.89353E-37"),result.getHits().get(0).getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getEvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getQvalue());
		assertEquals(Long.valueOf("24"),result.getHits().get(0).getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getTargetConsensus());
		assertEquals("+",result.getHits().get(0).getStrand());
	}

	@Test
	public void testCompareMotifsWithTomtomJobByCollectionFromWs() throws Exception {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();

		TomtomRunParameters paramsTomtom = new TomtomRunParameters();
		paramsTomtom.setDist("pearson");
		paramsTomtom.setThresh(0.00);
		paramsTomtom.setEvalue(0L);
		paramsTomtom.setInternal(0L);
		paramsTomtom.setMinOverlap(0L);
		paramsTomtom.setQueryRef(TEST_WORKSPACE + "/" + testCollectionId);
		paramsTomtom.setTargetRef(TEST_WORKSPACE + "/" + testCollectionId);

		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		String jobId = jobClient.createJob();

		System.out.println(jobId);
		assertNotNull(jobId);
		
		String resultId = MemeServerImpl.compareMotifsWithTomtomJobByCollectionFromWs(TEST_WORKSPACE, paramsTomtom, jobId, token.toString());

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		TomtomRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(TomtomRunResult.class);
		
		assertNotNull(result.getHits().get(0).getTargetPspmId());
		assertEquals(result.getHits().get(0).getTargetPspmId(),result.getHits().get(0).getQueryPspmId());
		assertEquals(Long.valueOf("0"),result.getHits().get(0).getOptimalOffset());
		assertEquals(Double.valueOf("3.89353E-37"),result.getHits().get(0).getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getEvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getQvalue());
		assertEquals(Long.valueOf("24"),result.getHits().get(0).getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getTargetConsensus());
		assertEquals("+",result.getHits().get(0).getStrand());
	}

	@Test
	public void testRunMast() throws IOException {
		String testFileName = "test/testoutput.txt";
		MemeServerImpl.executeCommand("mast test/mem_12345.txt test/seqs.fasta -hit_list -nostatus", testFileName);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(testFileName));
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				Runtime.getRuntime().exec("rm " + testFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertEquals("# All non-overlapping hits in all sequences from \"test/seqs.fasta\".",line);
	}

	
	@Test
	public void testGenerateMastCommandLine() {
		String result = MemeServerImpl.generateMastCommandLine("1.meme", "2.fasta", 0.0005, -1);
		assertEquals("mast 1.meme 2.fasta -mt 0.0005 -hit_list -nostatus",result);
	}
	
	@Test
	public void testGenerateHitMast() {
		String hit = "209110 +2 65 78  1527.00 2.53e-05";
		MastHit result = MemeServerImpl.generateHitMast(hit);
		assertEquals("209110", result.getSeqId());
		assertEquals("+", result.getStrand());
		assertEquals("2", result.getPspmId());
		assertEquals(Long.valueOf("65"), result.getHitStart());
		assertEquals(Long.valueOf("78"), result.getHitEnd());
		assertEquals(Double.valueOf("1527.00"), result.getScore());
		assertEquals(Double.valueOf("0.0000253"), result.getHitPvalue());
	}

	@Test
	public void testParseMastOutput() {
		String testOutputFile = "test/outmast.txt";
		List<MastHit> result = MemeServerImpl.parseMastOutput(testOutputFile);
		assertEquals("209110", result.get(0).getSeqId());
		assertEquals("+", result.get(0).getStrand());
		assertEquals("2", result.get(0).getPspmId());
		assertEquals(Long.valueOf("65"), result.get(0).getHitStart());
		assertEquals(Long.valueOf("78"), result.get(0).getHitEnd());
		assertEquals(Double.valueOf("1527.00"), result.get(0).getScore());
		assertEquals(Double.valueOf("0.0000253"), result.get(0).getHitPvalue());
	}


	@Test
	public void testFindSitesWithMastByCollection() throws Exception {

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testCollectionId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection memePspmCollection = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);

		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withPspmId(testMemePspmId);

		MastRunResult result = MemeServerImpl.findSitesWithMastByCollection(memePspmCollection, testSequenceSet, mastParams, null, null);
		System.out.println(result.toString());

		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.40", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("5.82E-10"), result.getHits().get(0).getHitPvalue());
	}

	
	@Test
	public void testFindSitesWithMastJobByCollectionFromWs() throws Exception {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		String jobId = jobClient.createJob();

		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withPspmId(testMemePspmId).withQueryRef(TEST_WORKSPACE + "/" + testCollectionId). withTargetRef(TEST_WORKSPACE + "/" + testSequenceSetId);
		
		String resultId = MemeServerImpl.findSitesWithMastJobByCollectionFromWs(TEST_WORKSPACE, mastParams, jobId, token.toString());

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MastRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MastRunResult.class);
		
		
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.40", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("5.82E-10"), result.getHits().get(0).getHitPvalue());
	}

	@Test
	public void testGetPspmCollectionFromMemeJobResultFromWs() throws Exception{

		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		String jobId = jobClient.createJob();

		String resultId = MemeServerImpl.getPspmCollectionFromMemeJobResultFromWs(TEST_WORKSPACE, TEST_WORKSPACE + "/" + testMemeRunResultId, jobId, token.toString());

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);

		assertNotNull(result);
		assertFalse(result.getPspms().size() == 0);
		assertEquals("ACGT", result.getAlphabet());

	}
	
	@Test
	public void testWsRead() throws Exception {

		AuthToken token = JsonClientCaller.requestTokenFromKBase(USER_NAME, PASSWORD.toCharArray());		

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testSequenceSetId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		SequenceSet result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(SequenceSet.class);
		
		System.err.println(result.toString());
		
		assertEquals("GCCGGGCACGGGCCACCTCATCATCCGAGACTGCGACGTCTTTCATGGGGTCTCCGGTTGCTCAAGTATGAGGGTACGATGCCTCCACTCCTGCCCCAAGTCCAGCCGTGCGTGAATGCGGTCACGTTCGTCACCATGAGGGTGACCGGGTTGCCGGGTGCGATACGCAGGGCTAACGCTGCCATAATCGGGAGAGGAGTATCCACGCTTCCGGTCATGCATCATCCACCCGCATCCGCAAGGAGGCCCC",result.getSequences().get(0).getSequence());
		assertEquals(testSequenceSet.getSequenceSetId(),result.getSequenceSetId());
	}

/*	@Test
	public void testWsWrite() throws Exception {
		
		String id = "";
		AuthToken token = JsonClientCaller.requestTokenFromKBase(USER_NAME, PASSWORD.toCharArray());
		MemeServerImpl.saveObjectToWorkspace (UObject.transformObjectToObject(testSequenceSet, UObject.class), "SequenceSet", WORKSPACE, id, token.toString());		
		fail("Not yet implemented"); // TODO
		
	}

	@Test
	public void testRegisterModule() throws Exception {
		//Now requestModuleOwnership
		
		AuthToken token = JsonClientCaller.requestTokenFromKBase(USER_NAME, PASSWORD.toCharArray());
		MemeServerImpl.wsClient(token.toString()).requestModuleOwnership("MEME");
		//WSUtil.saveObject(testSequenceSet.getSequenceSetId(), testSequenceSet, false);
		fail("Not yet implemented"); // TODO
		
	}
	
	@Test
	public void testListModules() throws Exception {
		
		AuthToken token = JsonClientCaller.requestTokenFromKBase(USER_NAME, PASSWORD.toCharArray());
		ListModulesParams params = new ListModulesParams();
		
		List<String> result = MemeServerImpl.wsClient(token.toString()).listModules(params);
		System.out.println(result.toString());
		//WSUtil.saveObject(testSequenceSet.getSequenceSetId(), testSequenceSet, false);
		fail("Not yet implemented"); // TODO
		
	}

	@Test
	public void testWsRegisterType() throws Exception {
		//Now CompileTypespec
		
		AuthToken token = JsonClientCaller.requestTokenFromKBase(USER_NAME, PASSWORD.toCharArray());
		CompileTypespecParams params = new CompileTypespecParams();
		params.setMod("MEME");
		List<String> types = new ArrayList<String>();
		types.add("SequenceSet");
		params.setNewTypes(types);
		Map<String,String> result = MemeServerImpl.wsClient(token.toString()).compileTypespec(params);
		System.out.println(result.toString());
		//WSUtil.saveObject(testSequenceSet.getSequenceSetId(), testSequenceSet, false);
		fail("Not yet implemented"); // TODO
		
	}

*/	
	@Test
	public void testFormatSequence() {
		String sequence = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaattt";
//		WSUtil.saveObject(id, testSequenceSet, false);
		String result = MemeServerImpl.formatSequence(sequence);
		assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\nttt",result);
	}
	
	public static void displayCollection(MemeRunResult collection) {
		System.out.println("KBase collection ID = "+collection.getId());
		System.out.println("timestamp = "+collection.getTimestamp());
		System.out.println("versionMeme = "+collection.getMemeVersion());
		System.out.println("inputDatafile = "+collection.getInputFileName());
		System.out.println("alphabetMeme = "+collection.getAlphabet());
		System.out.println("trainingSetMeme = "+collection.getTrainingSet().toString());
		System.out.println("commandLineMeme = "+collection.getCommandLine());
		System.out.println("modMeme = "+collection.getMod());
		System.out.println("nmotifsMeme = "+collection.getNmotifs());
		System.out.println("evtMeme = "+collection.getEvt());
		System.out.println("objectFunctionMeme = "+collection.getObjectFunction());
		System.out.println("minwMeme = "+collection.getMinw());
		System.out.println("maxwMeme = "+collection.getMaxw());
		System.out.println("minicMeme = "+collection.getMinic());
		System.out.println("wgMeme = "+collection.getWg());
		System.out.println("wsMeme = "+collection.getWs());
		System.out.println("endgapsMeme = "+collection.getEndgaps());
		System.out.println("minsitesMeme = "+collection.getMinsites());
		System.out.println("maxsitesMeme = "+collection.getMaxsites());
		System.out.println("wnsitesMeme = "+collection.getWnsites());
		System.out.println("probMeme = "+collection.getProb());
		System.out.println("spmapMeme = "+collection.getSpmap());
		System.out.println("spfuzzMeme = "+collection.getSpfuzz());
		System.out.println("substringMeme = "+collection.getSubstring());
		System.out.println("branchingMeme = "+collection.getBranching());
		System.out.println("wbranchMeme = "+collection.getWbranch());
		System.out.println("priorMeme = "+collection.getPrior());
		System.out.println("bMeme = "+collection.getB());
		System.out.println("maxiterMeme = "+collection.getMaxiter());
		System.out.println("distanceMeme = "+collection.getDistance());
		System.out.println("nMeme = "+collection.getN());
		System.out.println("nCapMeme = "+collection.getNCap());
		System.out.println("strandsMeme = "+collection.getStrands());
		System.out.println("seedMeme = "+collection.getSeed());
		System.out.println("seqfracMeme = "+collection.getSeqfrac());
		System.out.println("letterFreqMeme = "+collection.getLetterFreq());
		System.out.println("bgFreqMeme = "+collection.getBgFreq());
		System.out.println("\n"+collection.getRawOutput()+"\n");
		for (MemeMotif motif : collection.getMotifs()){
			System.out.println("\n"+motif.getRawOutput()+"\n");
			System.out.println("\tkbaseMotifMemeId = "+motif.getId());
			System.out.println("\tmotifName = "+motif.getDescription());
			System.out.println("\tmotifWidthMeme = "+motif.getWidth());
			System.out.println("\tmotifSitesMeme = "+motif.getSites());
			System.out.println("\tmotifLlrMeme = "+motif.getLlr());
			System.out.println("\tmotifEvalueMeme = "+motif.getEvalue());
			for (MemeSite site : motif.getSites()){
				System.out.println("\t\tsiteSequenceName = "+site.getSourceSequenceId());
				System.out.println("\t\tsiteStart = "+site.getStart());
				System.out.println("\t\tsitePvalue = "+site.getPvalue());
				System.out.println("\t\tsiteLeftFlank = "+site.getLeftFlank());
				System.out.println("\t\tsiteSequence = "+site.getSequence());
				System.out.println("\t\tsiteRightFlank = "+site.getRightFlank());
			}
		}
		
	}

	@Test
	public void testJobClientCreation() throws IOException, JsonClientException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, USER_NAME, PASSWORD);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		List<String> jobServicesList = client.listJobServices();
		assertNotNull(jobServicesList);
	}

	@Test
	public void testCreateJob() {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, USER_NAME, PASSWORD);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			String jobId = client.createJob();
			System.out.println(jobId);
			assertNotNull(jobId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testStartJob() throws AuthException, IOException, JsonClientException {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
			String status = "Starting...";
			String desc = "Test job";
			String jobId = "52ce016ee4b0b6f144e3db2f";

			InitProgress initProgress = new InitProgress();
			initProgress.setPtype("task");
			initProgress.setMax(2L);

			Date date = new Date();
			date.setTime(date.getTime()+100000L);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			
			URL jobServiceUrl = new URL(JOB_SERVICE);
			UserAndJobStateClient client = new UserAndJobStateClient(jobServiceUrl, USER_NAME, PASSWORD);
			client.startJob(jobId, token.toString(), status, desc, initProgress, dateFormat.format(date));
			System.out.println(jobId);
			assertNotNull(jobId);
	}

/*	@Test
	public void testUpdateJob() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			String status = "Running...";
			Date date = new Date();
			date.setTime(date.getTime()+10000L);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			client.updateJob(jobId, token.toString(), status, dateFormat.format(date));
			System.out.println(jobId);
			assertNotNull(jobId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateJobProgress() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			String status = "Running...";
			Date date = new Date();
			date.setTime(date.getTime()+10000L);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			client.updateJobProgress(jobId, token.toString(), status, 1L, dateFormat.format(date));
			System.out.println(jobId);
			assertNotNull(jobId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetJobInfo() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			Tuple14<String,String,String,String,String,String,Long,Long,String,String,Long,Long,String,Results> t = client.getJobInfo(jobId); 
			System.out.println(t.getE1());
			System.out.println(t.getE2());
			System.out.println(t.getE3());
			System.out.println(t.getE4());
			System.out.println(t.getE5());
			System.out.println(t.getE6());
			System.out.println(t.getE7());
			System.out.println(t.getE8());
			System.out.println(t.getE9());
			System.out.println(t.getE10());
			System.out.println(t.getE11());
			System.out.println(t.getE12());
			System.out.println(t.getE13());
			System.out.println(t.getE14());
			assertNotNull(t.getE1());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetJobDescription() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			Tuple5<String,String,Long,String,String> t = client.getJobDescription(jobId); 
			System.out.println(t.getE1());
			System.out.println(t.getE2());
			System.out.println(t.getE3());
			System.out.println(t.getE4());
			System.out.println(t.getE5());
			assertNotNull(t.getE1());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetJobStatus() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
			System.out.println(t.getE1());
			System.out.println(t.getE2());
			System.out.println(t.getE3());//Status
			System.out.println(t.getE4());
			System.out.println(t.getE5());
			System.out.println(t.getE6());
			System.out.println(t.getE7());
			assertNotNull(t.getE1());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testCompleteJob() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		try {
			String status = "Finished";
			String error = null;
			
			Results res = new Results();
			List<String> workspaceIds = new ArrayList<String>();
			workspaceIds.add(TEST_WORKSPACE);
			workspaceIds.add(testSequenceSetId);
			res.setWorkspaceids(workspaceIds);
			client.completeJob(jobId, token.toString(), status, error, res); 
			assertNotNull(jobId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetResults() throws AuthException, IOException {
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;
		//AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);

		try {
			Results res = client.getResults(jobId);			
			String workspaceId = res.getWorkspaceids().get(0);
			System.out.println(workspaceId);
			String resultId = res.getWorkspaceids().get(1);
			System.out.println(resultId);
			
			assertNotNull(res.getAdditionalProperties().keySet());
			assertEquals(workspaceId,TEST_WORKSPACE);
			assertEquals(resultId,testSequenceSetId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	@Test
	public void testDeleteJob() throws AuthException, IOException, UnauthorizedException, JsonClientException {
		String jobId = "53abab59e4b0796ed8e47141";

//		AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		AuthToken serviceToken = AuthService.login(MemeServerConfig.SERVICE_LOGIN, new String(MemeServerConfig.SERVICE_PASSWORD)).getToken();

		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.forceDeleteJob(serviceToken.toString(), jobId);
	}
	
/*	@Test
	public void testJsonCall() throws IOException, JsonClientException, AuthException {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
        Map<String, String> jsonArgs = new HashMap<String, String>();
        jsonArgs.put("target", "cloud");
        jsonArgs.put("application", "meme");
        jsonArgs.put("method", "find_motifs_with_meme_job_from_ws");
        jsonArgs.put("job_id", "12345");
        jsonArgs.put("workspace", "AKtest");
        jsonArgs.put("seq_id", testSequenceSetId);
        jsonArgs.put("mod", "oops");
        jsonArgs.put("nmotifs", "2");
        jsonArgs.put("minw", "14");
        jsonArgs.put("maxw", "24");
        jsonArgs.put("pal", "1");
        jsonArgs.put("revcomp", "0");
        jsonArgs.put("nsites", "0");
        jsonArgs.put("minsites", "0");
        jsonArgs.put("maxsites", "0");
        jsonArgs.put("token", token.toString());

        MemeServerCaller.setAuthAllowedForHttp(true);
		String result = MemeServerCaller.jsonCall(jsonArgs, token);
		System.out.println(result);

	}
	
	@Test
	public final void testGetKbaseId() throws Exception {
		String id = MemeServerImpl.getKbaseId("MemeMotif");
		System.out.println(id);
		assertNotNull(id);
	}
*/

	@Test
	public void testGetToken() throws AuthException, IOException, JsonClientException {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		System.out.println(token.toString());
		assertNotNull(token.toString());
	}

}

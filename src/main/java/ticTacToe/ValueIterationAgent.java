package ticTacToe;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Value Iteration Agent, only very partially implemented. The methods to implement are: 
 * (1) {@link ValueIterationAgent#iterate}
 * (2) {@link ValueIterationAgent#extractPolicy}
 * 
 * You may also want/need to edit {@link ValueIterationAgent#train} - feel free to do this, but you probably won't need to.
 * @author ae187
 *
 */
public class ValueIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states
	 */
	Map<Game, Double> valueFunction=new HashMap<Game, Double>();
	
	/**
	 * the discount factor
	 */
	double discount=0.9;
	
	/**
	 * the MDP model
	 */
	TTTMDP mdp=new TTTMDP();
	
	/**
	 * the number of iterations to perform - feel free to change this/try out different numbers of iterations
	 */
	int k=10;
	
	
	/**
	 * This constructor trains the agent offline first and sets its policy
	 */
	public ValueIterationAgent()
	{
		super();
		mdp=new TTTMDP();
		this.discount=0.9;
		initValues();
		train();
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public ValueIterationAgent(Policy p) {
		super(p);
		
	}

	public ValueIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		mdp=new TTTMDP();
		initValues();
		train();
	}
	
	/**
	 * Initialises the {@link ValueIterationAgent#valueFunction} map, and sets the initial value of all states to 0 
	 * (V0 from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.valueFunction.put(g, 0.0);
		
		
		
	}
	
	
	
	public ValueIterationAgent(double discountFactor, double winReward, double loseReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		mdp=new TTTMDP(winReward, loseReward, livingReward, drawReward);
		
	}
	
	/**
	 
	
	/*
	 * Performs {@link #k} value iteration steps. After running this method, the {@link ValueIterationAgent#valueFunction} map should contain
	 * the (current) values of each reachable state. You should use the {@link TTTMDP} provided to do this.
	 * 
	 *
	 */
	public void iterate()
	{
		/* YOUR CODE HERE
		 */
		//get how many time steps
		int j =k;
		//loop for how many time steps
		while(j!=0 && j> 0) {
			//gets states
			Set<Game> games =valueFunction.keySet();
			//loop for all states
			for(Game g: games){
				//make sure game is on going
				if(!g.isTerminal())
				{
					//get all possible moves from each state
					List<Move> moves = g.getPossibleMoves();
					//set the max to a really small number
					double max = -100;
					//loop for how many moves there is from that state
					for(Move m: moves) {
						//get transition objects for that state
						List<TransitionProb> outcomes = mdp.generateTransitions(g, m);
						//initialise result to 0;
						double result = 0;
						//loop for how many transition objects there is 
						for(int i =0;i < outcomes.size();i++) {
							//get reward 
							double reward = outcomes.get(i).outcome.localReward;
							//get probability
							double prob = outcomes.get(i).prob;
							//get next state 
							Game sPrime = outcomes.get(i).outcome.sPrime;
							//calculate the result and add it to previous 
							result = result+( prob*(reward + (this.discount*valueFunction.get(sPrime))));
							
						}
						//if max is lower than result 
						if(max < result) {
							//set new max to result
							max = result;
						}
						//add to hashmap 
						this.valueFunction.put(g, max);
					}
				}
					
				
				
			}
			j--;
		}		
	}
	
	
	/**This method should be run AFTER the train method to extract a policy according to {@link ValueIterationAgent#valueFunction}
	 * You will need to do a single step of expectimax from each game (state) key in {@link ValueIterationAgent#valueFunction} 
	 * to extract a policy.
	 * 
	 * @return the policy according to {@link ValueIterationAgent#valueFunction}
	 */
	public Policy extractPolicy()
	{
		
		Policy policy = new Policy();
		Set<Game> games = valueFunction.keySet();
		for(Game g: games) {
			List<Move> moves = g.getPossibleMoves();
			double max = -100;
			Move bestmove = null;
			for(Move m: moves) {
				List<TransitionProb> outcomes = mdp.generateTransitions(g, m);
				double result = 0;
				
				
				for(int i =0;i < outcomes.size();i++) {
					
					
					double reward = outcomes.get(i).outcome.localReward;
					
					double prob = outcomes.get(i).prob;
				
					Game sPrime = outcomes.get(i).outcome.sPrime;
					
					result = result +(prob*(reward + (this.discount*this.valueFunction.get(sPrime))));
					 
				}
				if(max < result) {
					max = result;
					bestmove = m;
					
				}
				
				policy.policy.put(g, bestmove);
				
			}
		}
		
		return policy;
	}
	
	/**
	 * This method solves the mdp using your implementation of {@link ValueIterationAgent#extractPolicy} and
	 * {@link ValueIterationAgent#iterate}. 
	 */
	public void train()
	{
		/**
		 * First run value iteration
		 */
		this.iterate();
		/**
		 * now extract policy from the values in {@link ValueIterationAgent#valueFunction} and set the agent's policy 
		 *  
		 */
		
		super.policy=extractPolicy();
		
		if (this.policy==null)
		{
			System.out.println("Unimplemented methods! First implement the iterate() & extractPolicy() methods");
			//System.exit(1);
		}
		
		
		
	}

	public static void main(String a[]) throws IllegalMoveException
	{
		//Test method to play the agent against a human agent.
		ValueIterationAgent agent=new ValueIterationAgent();
		HumanAgent d=new HumanAgent();
		
		Game g=new Game(agent, d, d);
		g.playOut();
		
		
		

		
		
	}
}

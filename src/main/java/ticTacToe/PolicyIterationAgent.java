package ticTacToe;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
/**
 * A policy iteration agent. You should implement the following methods:
 * (1) {@link PolicyIterationAgent#evaluatePolicy}: this is the policy evaluation step from your lectures
 * (2) {@link PolicyIterationAgent#improvePolicy}: this is the policy improvement step from your lectures
 * (3) {@link PolicyIterationAgent#train}: this is a method that should runs/alternate (1) and (2) until convergence. 
 * 
 * NOTE: there are two types of convergence involved in Policy Iteration: Convergence of the Values of the current policy, 
 * and Convergence of the current policy to the optimal policy.
 * The former happens when the values of the current policy no longer improve by much (i.e. the maximum improvement is less than 
 * some small delta). The latter happens when the policy improvement step no longer updates the policy, i.e. the current policy 
 * is already optimal. The algorithm should stop when this happens.
 * 
 * @author ae187
 *
 */
public class PolicyIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states according to the current policy (policy evaluation). 
	 */
	HashMap<Game, Double> policyValues=new HashMap<Game, Double>();
	
	/**
	 * This stores the current policy as a map from {@link Game}s to {@link Move}. 
	 */
	HashMap<Game, Move> curPolicy=new HashMap<Game, Move>();
	
	double discount=0.9;
	
	/**
	 * The mdp model used, see {@link TTTMDP}
	 */
	TTTMDP mdp;
	
	/**
	 * loads the policy from file if one exists. Policies should be stored in .pol files directly under the project folder.
	 */
	public PolicyIterationAgent() {
		super();
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
		
		
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public PolicyIterationAgent(Policy p) {
		super(p);
		
	}

	/**
	 * Use this constructor to initialise a learning agent with default MDP paramters (rewards, transitions, etc) as specified in 
	 * {@link TTTMDP}
	 * @param discountFactor
	 */
	public PolicyIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Use this constructor to set the various parameters of the Tic-Tac-Toe MDP
	 * @param discountFactor
	 * @param winningReward
	 * @param losingReward
	 * @param livingReward
	 * @param drawReward
	 */
	public PolicyIterationAgent(double discountFactor, double winningReward, double losingReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		this.mdp=new TTTMDP(winningReward, losingReward, livingReward, drawReward);
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Initialises the {@link #policyValues} map, and sets the initial value of all states to 0 
	 * (V0 under some policy pi ({@link #curPolicy} from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.policyValues.put(g, 0.0);
		
	}
	
	/**
	 *  You should implement this method to initially generate a random policy, i.e. fill the {@link #curPolicy} for every state. Take care that the moves you choose
	 *  for each state ARE VALID. You can use the {@link Game#getPossibleMoves()} method to get a list of valid moves and choose 
	 *  randomly between them. 
	 */
	public void initRandomPolicy()
	{
		//instance of random
		Random rand = new Random();
		//get set of states
		Set<Game> games = policyValues.keySet();
		//initialise rand number 
		int randN = 0;
		//loop for every state
		for(Game g: games) {
			//check the game is ongoing
			int gameStatus = g.evaluateGameState();
			if(gameStatus == 0) {
				//get list of valid moves
				List<Move> moves = g.getPossibleMoves();
				//generate ran number within size of moves list 
				randN = rand.nextInt(moves.size());
				//get random move
				Move move = moves.get(randN);
				//set current policy of that state to random move
				curPolicy.put(g, move);
			}
			
			
		}
		
	}
	
	
	/**
	 * Performs policy evaluation steps until the maximum change in values is less than {@code delta}, in other words
	 * until the values under the current policy converge. After running this method, 
	 * the {@link PolicyIterationAgent#policyValues} map should contain the values of each reachable state under the current policy. 
	 * You should use the {@link TTTMDP} {@link PolicyIterationAgent#mdp} provided to do this.
	 *
	 * @param delta
	 */
	protected void evaluatePolicy(double delta)
	{
		//get set of games states
		Set<Game> games = curPolicy.keySet();
		//create boolean for if change is less than delta
		Boolean lessDelta = false;
		//while change is still not less than delta
		while(lessDelta == false) {
			//set max change to something very small
			double maxChange = -99999;
			//loop for all games
			for(Game g: games) {
				//check the game is ongoing
				if(!g.isTerminal())
				{
					//generate game transition objects
					List<TransitionProb> outcomes = mdp.generateTransitions(g, curPolicy.get(g));
					//initialise and reset result for every game 
					double result = 0;
					//loop for how many outcomes there is
					for(int i =0;i < outcomes.size();i++) {
						//get reward 
						double reward = outcomes.get(i).outcome.localReward;
						//get probability
						double prob = outcomes.get(i).prob;
						//get next state 
						Game sPrime = outcomes.get(i).outcome.sPrime;
						//calculate the result and add it to previous 
						result = result+( prob*(reward + (this.discount*policyValues.get(sPrime))));
						
						}
					//Initialise change to result take away previous result
					double change = (result - policyValues.get(g));
					
					
					
					//if change is a negative
					//if(change < 0) {
						//disregard the negative if change is greater than maxchange 
						//if ((-change) > maxChange) {
							//set maxchange to the new change 
							//maxChange = (-change);
						//}
					//}else {
						//change must already be positve 
					
					
					
					
					//if change is greater than maxchange 
					if(change > maxChange) {
							//set maxchange to change
						maxChange = change;
					}
					//}
					//add to hashmap 
					this.policyValues.put(g, result);
				}
				else {
					//if terminal set value to zero
					this.policyValues.put(g, 0.0);
				}
				
				
			
			}
			//if make change is smaller than delta stop while loop
			
			if(maxChange < delta) {
				
				lessDelta = true;	
			}	
		}
	}
		
		
	
	
	/**This method should be run AFTER the {@link PolicyIterationAgent#evaluatePolicy} train method to improve the current policy according to 
	 * {@link PolicyIterationAgent#policyValues}. You will need to do a single step of expectimax from each game (state) key in {@link PolicyIterationAgent#curPolicy} 
	 * to look for a move/action that potentially improves the current policy. 
	 * 
	 * @return true if the policy improved. Returns false if there was no improvement, i.e. the policy already returned the optimal actions.
	 */
	protected boolean improvePolicy()
	{
		boolean hasimproved = false;
		
		//get set of game states
		Set<Game> games = curPolicy.keySet();
		//loop for game in games
		for(Game g: games) {
			//get list of possible moves for state
			List<Move> moves = g.getPossibleMoves();
			//set max to really low number
			double max = -99999;
			//Initialise bestmove 
			Move bestmove = null;
			//loop for all moves 
			for(Move m: moves) {
				//get transition objects
				List<TransitionProb> outcomes = mdp.generateTransitions(g, m);
				//reset result to 0
				double result = 0;
				
				//loop for all transition objects
				for(int i =0;i < outcomes.size();i++) {
					
					//get reward
					double reward = outcomes.get(i).outcome.localReward;
					//get probability 
					double prob = outcomes.get(i).prob;
					//get target state
					Game sPrime = outcomes.get(i).outcome.sPrime;
					//get result
					result = result +(prob*(reward + (this.discount*this.policyValues.get(sPrime))));
					 
				}
				//if max is smaller than result set max to result and set bestmove to current move
				if(max < result) {
					max = result;
					bestmove = m;
					
				}
				
			}
			//make sure policy has improved
			if(max > this.policyValues.get(g)) {
				// add new action to current policy 
				this.curPolicy.put(g, bestmove);
				//flag there has been an improvement 
				hasimproved = true;
			}
			
				
		}
		//if there has been an improvement
		if(hasimproved) {
			//return true 
			return true;
		}else {
			//return false 
			return false;
		}
	}
	
	/**
	 * The (convergence) delta
	 */
	double delta=0.1;
	
	/**
	 * This method should perform policy evaluation and policy improvement steps until convergence (i.e. until the policy
	 * no longer changes), and so uses your 
	 * {@link PolicyIterationAgent#evaluatePolicy} and {@link PolicyIterationAgent#improvePolicy} methods.
	 */
	public void train()
	{
		//create boolean for to keep track of convergence 
		boolean notConvergence = true;
		//loop while convergence is false
		while(notConvergence) {
			//Evaluate policy
			evaluatePolicy(this.delta);
			//set notconvergence to improved  policy
			//when there is no more improvements to be made, convergence will be false loop will terminate
			notConvergence = improvePolicy();
		}
		//set agent policy to the current policy after training
		super.policy = new Policy(curPolicy);
	}
	
	public static void main(String[] args) throws IllegalMoveException
	{
		/**
		 * Test code to run the Policy Iteration Agent agains a Human Agent.
		 */
		PolicyIterationAgent pi=new PolicyIterationAgent();
		
		HumanAgent h=new HumanAgent();
		
		Game g=new Game(pi, h, h);
		
		g.playOut();
		
		
	}
	

}

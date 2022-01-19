package ticTacToe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

/**
 * A Q-Learning agent with a Q-Table, i.e. a table of Q-Values. This table is implemented in the {@link QTable} class.
 * 
 *  The methods to implement are: 
 * (1) {@link QLearningAgent#train}
 * (2) {@link QLearningAgent#extractPolicy}
 * 
 * Your agent acts in a {@link TTTEnvironment} which provides the method {@link TTTEnvironment#executeMove} which returns an {@link Outcome} object, in other words
 * an [s,a,r,s']: source state, action taken, reward received, and the target state after the opponent has played their move. You may want/need to edit
 * {@link TTTEnvironment} - but you probably won't need to.
 * @author ae187
 */

public class QLearningAgent extends Agent {
	
	/**
	 * The learning rate, between 0 and 1.
	 */
	double alpha=0.2;
	
	/**
	 * The number of episodes to train for
	 */
	int numEpisodes=30000;
	
	/**
	 * The discount factor (gamma)
	 */
	double discount=0.9;
	
	
	/**
	 * The epsilon in the epsilon greedy policy used during training.
	 */
	double epsilon=0.2;
	
	/**
	 * This is the Q-Table. To get an value for an (s,a) pair, i.e. a (game, move) pair, you can do
	 * qTable.get(game).get(move) which return the Q(game,move) value stored. Be careful with 
	 * cases where there is currently no value. You can use the containsKey method to check if the mapping is there.
	 * 
	 */
	
	QTable qTable=new QTable();
	
	
	/**
	 * This is the Reinforcement Learning environment that this agent will interact with when it is training.
	 * By default, the opponent is the random agent which should make your q learning agent learn the same policy 
	 * as your value iteration and policy iteration agents.
	 */
	TTTEnvironment env=new TTTEnvironment();
	
	
	/**
	 * Construct a Q-Learning agent that learns from interactions with {@code opponent}.
	 * @param opponent the opponent agent that this Q-Learning agent will interact with to learn.
	 * @param learningRate This is the rate at which the agent learns. Alpha from your lectures.
	 * @param numEpisodes The number of episodes (games) to train for
	 */
	public QLearningAgent(Agent opponent, double learningRate, int numEpisodes, double discount)
	{
		env=new TTTEnvironment(opponent);
		this.alpha=learningRate;
		this.numEpisodes=numEpisodes;
		this.discount=discount;
		initQTable();
		train();
	}
	
	/**
	 * Initialises all valid q-values -- Q(g,m) -- to 0.
	 *  
	 */
	
	protected void initQTable()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
		{
			List<Move> moves=g.getPossibleMoves();
			for(Move m: moves)
			{
				this.qTable.addQValue(g, m, 0.0);
				//System.out.println("initing q value. Game:"+g);
				//System.out.println("Move:"+m);
			}
			
		}
		
	}
	
	/**
	 * Uses default parameters for the opponent (a RandomAgent) and the learning rate (0.2). Use other constructor to set these manually.
	 */
	public QLearningAgent()
	{
		this(new RandomAgent(), 0.2, 30000, 0.9);
		
	}
	
	/**
	 * Returns the entry with the max q value
	 * 
	 * @param state which is a Game
	 * @return HashMap.Entry<Move, Double> 
	 */
	public HashMap.Entry<Move, Double> maxQ(Game state) {
		if(!state.isTerminal()) 
		{
			HashMap<Move, Double> moveQs = qTable.get(state);
			Double maxQ = -Double.MAX_VALUE;
			HashMap.Entry<Move, Double> maxEntry = null;
		
			for (HashMap.Entry<Move, Double> q : moveQs.entrySet())
			{
				if (q.getValue() > maxQ)
			    	{
						
				 		maxQ = q.getValue();
				 		maxEntry = q;
			    	}
			}
			return maxEntry;
		}
		return null;
		
	}
	
	/**
	 *  Implement this method. It should play {@code this.numEpisodes} episodes of Tic-Tac-Toe with the TTTEnvironment, updating q-values according 
	 *  to the Q-Learning algorithm as required. The agent should play according to an epsilon-greedy policy where with the probability {@code epsilon} the
	 *  agent explores, and with probability {@code 1-epsilon}, it exploits. 
	 *  
	 *  At the end of this method you should always call the {@code extractPolicy()} method to extract the policy from the learned q-values. This is currently
	 *  done for you on the last line of the method.
	 *  
	 */
	
	public void train() 
	{	
		
		//get number of episodes
		int epi = this.numEpisodes;
		
		
		
	
		//used for debugging
		int i =0;
		//while there is still episodes to be played
		while(epi > 0) {
			i++;
			//set the training environment to be the same as the normal environment
			TTTEnvironment trainingE = new TTTEnvironment();
			
			
			//while g is not terminal 
			while(!trainingE.game.isTerminal()) {
				//set trainingE
				Game g = trainingE.game;
				//generate random number
				double z = Math.random();
				//initialise sample, currentAvg, outcome, reward, maxQ and sPrime to null
				double sample = 0;
				double currentAvg = 0;
				Outcome outcome = null;
				Game previousS = null;
				double reward = 0;
				Game sPrime = null;
				HashMap.Entry<Move, Double> maxQ = null;
				
				
				//if random number is less than epsilon explore
				if(z <= epsilon)
				{
					//get all possible moves from state
					List<Move> moves = g.getPossibleMoves();
					//set the maxMove to the last move in moves
					int maxMove = moves.size()-1;
					//set Min to  the first move in move
					int min = 0;
					//generate random index using math, which is inside the range of moves
					int x = (int)(Math.random() * (maxMove- min +1)+ min);
				
					//select move at this random index
					Move m = moves.get(x);
					
					//if the move is legal
					if(g.isLegal(m)) 
					{
						//execute move and set to outcome
						try {
							outcome = trainingE.executeMove(m);
							
						} catch (IllegalMoveException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
						}
						//get local reward
						reward = outcome.localReward;
						//get s prime
						sPrime = outcome.sPrime;
						//get previous state
						previousS = outcome.s;
						//if S prime is not terminal
						if(!sPrime.isTerminal())
						{
							//get max q value 
							maxQ = maxQ(sPrime);
							//calculate sample
							sample = (reward + this.discount*(maxQ.getValue()));
							//get the new current average using the old average, learning rate and sample
							currentAvg = ((1-this.alpha)*qTable.getQValue(previousS, m)) +(this.alpha*sample);
							
							
						}
						//s prime is terminal
						else {
							//maxQ of sprime will be 0 as its termainl
							sample = (reward + 0);
							//get current average using the old average, learning rate and sample
							currentAvg = ((1-this.alpha)*qTable.getQValue(previousS, m)) + (this.alpha*sample);
							//update q value for that move 	
							qTable.get(previousS).replace(m, currentAvg);
							break;
						}
						//update q value for that move 	
						qTable.get(previousS).replace(m, currentAvg);
						
								
								
						}
							
						
					}
					else {
						//get action where q is max
						maxQ = maxQ(g);
						//check its legal move and agents turn
						if(g.isLegal(maxQ.getKey()))
						{
							//execute move
							try {
								outcome = trainingE.executeMove(maxQ.getKey());
								
							} catch (IllegalMoveException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//get s prime and local reward 
							reward = outcome.localReward;
							sPrime = outcome.sPrime;
							previousS = outcome.s;
							//id s prime is not terminal
							if((!sPrime.isTerminal()))
							{
								//get max q value for s prime
								HashMap.Entry<Move, Double> primeMaxQ = maxQ(sPrime);		
								//calculate sample
								sample = (reward + this.discount*(primeMaxQ.getValue()));
								//get the new current average using the old average, learning rate and sample
								currentAvg = ((1-this.alpha)*qTable.getQValue(previousS, maxQ.getKey())) +(this.alpha*sample);
								
								
							}
							else {
								//sample is reward pluss 0 as sprime is terminal
								sample =(reward +0);
								//get the new current average using the old average, learning rate and sample
								currentAvg = ((1-this.alpha)*qTable.getQValue(previousS, maxQ.getKey())) +(this.alpha*sample);
								qTable.get(previousS).replace(maxQ.getKey(), currentAvg);
								break;

							}
							
							//update q value for that move 	
							qTable.get(previousS).replace(maxQ.getKey(), currentAvg);
							
						}
					
					}
					
			}
			epi--;
		}
		 
		System.out.println("the num of epi " + i);
		
		
		//--------------------------------------------------------
		//you shouldn't need to delete the following lines of code.
		this.policy=extractPolicy();
		if (this.policy==null)
		{
			System.out.println("Unimplemented methods! First implement the train() & extractPolicy methods");
			//System.exit(1);
		}
	}
	
	/** Implement this method. It should use the q-values in the {@code qTable} to extract a policy and return it.
	 *
	 * @return the policy currently inherent in the QTable
	 */
	public Policy extractPolicy()
	{
		//create policy
		Policy bestPolicy = new Policy();
		//loop for all moves for a state
		for(HashMap.Entry<Game, HashMap<Move,Double>> g: qTable.entrySet()) {
			//get max q value action for state
			Entry<Move, Double> maxQ = maxQ(g.getKey());
			//add it to policy
			bestPolicy.policy.put(g.getKey(), maxQ.getKey());
			
			
		}
		
		
		//return the policy
		return bestPolicy;
		
	}
	
	public static void main(String a[]) throws IllegalMoveException
	{
		//Test method to play your agent against a human agent (yourself).
		QLearningAgent agent=new QLearningAgent();
		
		HumanAgent d=new HumanAgent();
		
		Game g=new Game(agent, d, d);
		g.playOut();
		
		
		

		
		
	}
	
	
	


	
}

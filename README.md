# Privacy Preserving Frequent ItemSet Generation
Recent Technological advances, such as smart speakers, IP cameras and other network-connected smart home devices, have significantly increased the capability of commercial companies to store the personal data of consumers.This has raised concerns that this personal data will be viewed by unauthorized people, or even sold to other companies for a profit. In order to deal with these concerns, several privacy preserving data mining methods have been proposed in recent years.
In this Work, two privacy preserving algorithms for finding the frequent Itemsets in a vertically partitioned database were compared, both of which are based on dot product computation. These algorithms are:

1)	The Vaidya-Clifton Private Scalar Product Protocol.  
2)	The Homomorphic encryption based scalar product protocol proposed by Goethals et al.

For further information on the algorithms see the attached Readme.Docx .

Due to the nature of the scalar dot product, both algorithms are generally limited to two-party interaction, and therefore were examined in a two-party setting, using 2 different databases and 3 values of support.  

## Implementation

The code is written in Java using the following dependencies:
1)	The Two-party communication is facilitated by Akka .
2)	The homomorphic encryption is based on the Paillier Cryptosystem. 

Alice and Bob are the main Classes of the software implementation and both of them implement the Akka actor interface. In order to facilitate communication between them, a variety of Message Classes were defined as well. 

The main functions for each actor are as follows: 

### Alice
	**Generate_f1_alice**: initialization of Apriori algorithm - finding all frequent-1 Itemsets in the DB
	**Generate_candidates**: generates the set of candidates k-itemsets, from F_(k-1), the set of frequent (k-1)-itemsets found in the previous step
	**Candidate_pruning**: prune the given candidates based on the following logic: a necessary condition of candidate to be frequent is that each of its (k-1)-itemset is frequent (The Apriori condition).
	**count_freqs**: for each itemset candidate - split it into frequencies that can and cannot be computed locally.
	**count_freq**: count the frequency of the itemset given as input via one of three methods: 
	 if the itemset contains only local vectors - compute it locally.
	 if the itemset contains only remote vectors - send a request to Bob for remote computation.
	if the itemset contains both local as well as remote vectors - invoke the selected privacy preserving communication algorithm.
	**update_freq**: add the given itemset to F_k if its frequency is larger than the minimum support. once finished checking all candidates, proceed to next step of algorithm - generating C_(k+1). 
	**generate_A_matrix**: Generate a matrix of values that form coefficients of linear independent equations by: 
	generating a matrix with random numbers - rand_a .
	For every row i, replace the diagonal element with the sum of the absolute values of elements of the corresponding row in rand_a (the remaining values remain unchanged). 
	such a matrix is diagonally dominant => is nonsingular => Linearly independent rows
	**comm_clifton & calc_freq_vc**:  used to send the X’ vector to bob and perform the dot product calculation upon receiving S,Y’. 
	**comm_homomorphic & decrypt_homomorphic**: used to send the encrypted X vector to bob and to decrypt the dot product received from him. 
### Bob
	**generate_f1_Bob**: initialization of Apriori algorithm - finding all frequent-1 Itemsets in the DB. Sends the results to Alice. 
	**count_freq**: used to calculate the frequency for local itemsets.
	**count_freq_vc & count_freq_homomorphic**: used to calculate the local part of a shared itemset according to the relevant algorithm. 
### General 
	**Paillier**: the implementation of the Paillier cryptosystem used in the homomorphic algorithm. Includes encrypt/dycript methods. 
	
## Running the code

run Bobmain, then Alicemain, and then follow the onscreen prompts. 
type /shutdown into both the alice and bob console to terminate the program. 
	

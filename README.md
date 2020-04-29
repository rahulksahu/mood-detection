# mood-detection
A simple java program which detects Ackman's 6 basic emotions from any text input, using feature vectors.

### Emotional Annotations
To check the accuracy of any emotion detecting algorithm, the results need to be compared to a human-labeled text. The process in which humans manually label a text is called annotation. Annotation can be done on multiple levels: word, sentence, paragraph, section, or even the entire document. In emotion detection studies, text is annotated on polarity, emotion, and intensity. When annotating on polarity, text is labeled with positive, negative, or neutral emotion. Text annotated on emotion is labeled as one of the listed emotions defined in the study. This project utilizes annotated text dataset provided by SemEval, 2007-Task. It has a prepared list of seed words for six basic emotion categories proposed by Ekman. These categories represent the distinctly identifiable facial expressions of emotion – happiness, sadness, anger, disgust, surprise and fear. I took words commonly used in the context of a particular emotion. Thus, I chose “happy”, “enjoy”, “pleased” as seed words for the happiness category, “afraid”, “scared”, “panic” for the fear category, and so on. For other categories of emotions, I listed them under “other” category. 

### Emotion Lexicon
The first step in detecting emotion in text is discovering keywords or phrases that associate with emotions. These words are important to training machine-learning algorithms. A list of emotions and words that express each emotion is called an emotional lexicon. In general, these lists start with identifying seed words, or words that highly associate with one emotion, and expand by using synonyms. I only considered single word keywords (unigrams) for making emotional lexicon, because using phrases (i.e. bigram, trigrams etc.) was causing too much overhead and slow procedure on algorithm. I used WordNet Dictionary and manually created collection of words, each annotated with the emotions they evoke. The creation process involved annotating a few seed words with Ekman's six basic emotions then expanding the collection by marking the WordNet synonyms of each word with the same emotion. The full list reached a few hundred words. 

### Word Lexicon
Apart from standard emotional keywords, which represent only a single emotion, we also have words which may represent more than two emotions in different contexts. Thus I listed those words (picked from WordNet Dictionary) with a set of emotions that can be represented by them, out of Ekman’s six basic emotions. 
### Acronyms
People generally use short form of original words while texting/chatting, called acronyms, but we can’t detect emotions out of these words if we don’t convert them to their usual/original form. So, I created a list of some most frequent/famous used acronyms and along with them I listed their original words. 

### Stop Words
Stop words are usually the most frequent words including articles (a, an, the), auxiliary verbs (be, am, is, are), prepositions (in, on, of, at), conjunctions (and, or, nor, when, while) that do not provide additional improvement for search engines but increase the computational complexity by increasing the size of the dictionary. The important aspect of stop-word removal in emotion detection is the words, not their frequencies. I used a list of stop words available online to identify them in our dataset and remove properly. 

### Amplifying Words
Amplifying words are words which themselves do not have any emotional feature, but together with other emotional words, they can amplify the impact of that emotion. For example: - very, a little, so, tremendous etc. These words have positive effects as they only increase the impact of emotion in positive manner. I used a set of amplifying words, picked from web, and annotate them with suitable amplifying factor, according to previous research works done on them. 

### Emoticons
Users use features of smileys/emotion-icons available in smartphone to express their mood/emotion while texting. So I mapped all general emoticons to their proper standard emotions so that we can detect the emotion of a text correctly which contains emoticons. 
Effect of Tense I also considered the effect of tense on user’s mood/emotions, because it is very necessary to identify if emotion detected in text belongs to current state, past state or future state of user, so that we can accordingly a list of emotions of user at different time spans. 

### Effect of Capitalization and Repetition
People use capitalized words and repetated letters in words in texting when they want to put more stress on those words, which gives more impact for those words and their emotions. Thats why I took into consideration this and gave proper weight to these effects. 
Negation Whenever we use a negation before any emotional keyword, it inverts the emotion of user. But the impact of that emotion is not as high as it should have been for its just opposite emotion. I took this into consideration and took suitable measures.

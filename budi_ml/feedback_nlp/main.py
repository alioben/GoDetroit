import nltk
import string
import pandas as pd
import numpy as np
from gibberish_detector import gib_detect_train
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB

'''
FEEDBACK ALGORITHM PIPELINE:

DETECT GIBBERISH(SOME RANDOM SOUND) ----> NAIVE BAYES
'''

'''
READ FILE, TRANSFORM TRAINING DATA INTO TF-IDF
'''
report_df = pd.read_csv('data/final_training_set_3.csv')
X = np.array(report_df['REPORT'])
y = np.array(report_df['CATEGORY'])


'''
Gibberish Classifier
'''
with open('gibberish_detector/gib_model.pickle', 'rb') as f:
    gibberish_clf = pickle.load(f)
gibberish_threshold = gibberish_clf['thresh']

'''
Naive Bayes Classifier
'''
# these 2 functions remove punctuation, lowercase, stem. They are used by the NB
stemmer = nltk.stem.porter.PorterStemmer()
remove_punctuation_map = dict((ord(char), None) for char in string.punctuation)
def stem_tokens(tokens):
    return [stemmer.stem(item) for item in tokens]

def normalize(text):
    return stem_tokens(nltk.word_tokenize(text.lower().translate(remove_punctuation_map)))

vectorizer = TfidfVectorizer(tokenizer=normalize, stop_words='english')
X = vectorizer.fit_transform(X)
clf = MultinomialNB().fit(X, y)


print 'The training is done.. Time to have some FUN'


while True:
    print 'Please input an example report by a citizen'
    string = raw_input()
    # First pipeline
    gibberish = not gib_detect_train.avg_transition_prob(string, gibberish_clf['mat']) > gibberish_threshold
    print 'gibberish? ', gibberish
    if not gibberish:
        # Second pipeline
        string = [str(string)]
        string = vectorizer.transform(np.array(string))
        result = clf.predict(string)
    else:
        result = [0]
    result = 'IMPORTANT' if result[0] == 1 else 'NOT IMPORTANT'
    print 'According to our predictive algorithm, this report is ', result

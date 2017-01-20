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

DETECT GIBBERISH(SOME RANDOM SOUND) ----> USE NAIVE BAYES CLASSIFIER
'''

'''
READ FILE, TRANSFORM TRAINING DATA INTO TF-IDF
'''
report_df = pd.read_csv('data/final_training_set.csv')
X = np.array(report_df['REPORT'])
y = np.array(report_df['CATEGORY'])


'''
Gibberish Classifier
'''
# Pickle no 1: gibberish classifier
with open('gibberish_detector/gib_model.pickle', 'rb') as f:
    gibberish_clf = pickle.load(f)[0]
gibberish_threshold = gibberish_clf['thresh']


def determine_gibberish(sentence):
    return not gib_detect_train.avg_transition_prob(string, gibberish_clf['mat']) > gibberish_threshold


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

# Pickle no 2: tfidf_vectorizer
tfidf_vectorizer = None
try:
    with open('tfidf_vectorizer.pickle', 'rb') as f:
        print 'tfidf_vectorizer is already there, just load it...'
        clf = pickle.load(f)[0]
except:
    pass

if not tfidf_vectorizer:
    print 'Fitting right now using the training dataset: '
    tfidf_vectorizer = TfidfVectorizer(tokenizer=normalize, stop_words='english')
    with open('tfidf_vectorizer.pickle', 'wb') as f:
        pickle.dump([tfidf_vectorizer], f)

X = tfidf_vectorizer.fit_transform(X)

# Pickle no 3: Naive Bayes Classifier
nb_clf = None
try:
    with open('nb_clf.pickle', 'rb') as f:
        print 'nb_clf, just load it...'
        clf = pickle.load(f)[0]
except:
    pass

if not nb_clf:
    print 'Training NB classifier using the trained tfidf_vectorizer '
    nb_clf = MultinomialNB().fit(X, y)
    with open('nb_clf.pickle', 'wb') as f:
        pickle.dump([nb_clf], f)


print 'The training is done.. Time to have some FUN'

# Ali, just see this section on how to use the models
while True:
    print 'Please input an example report by a citizen'
    string = raw_input()
    # First pipeline
    gibberish = determine_gibberish(string)
    print 'gibberish? ', gibberish
    if not gibberish:
        # Second pipeline
        string = [str(string)]
        string = tfidf_vectorizer.transform(np.array(string))
        result = nb_clf.predict(string)
    else:
        result = [0]
    result = 'IMPORTANT' if result[0] == 1 else 'NOT IMPORTANT'
    print 'According to our predictive algorithm, this report is ', result

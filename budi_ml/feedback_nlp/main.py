import nltk
import string
import pandas as pd
import numpy as np
import sys
from gibberish_detector import gib_detect_train
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB

'''
FEEDBACK ALGORITHM PIPELINE:

DETECT GIBBERISH(SOME RANDOM SOUND) ----> USE NAIVE BAYES CLASSIFIER
'''


def determine_gibberish(sentence):
    '''
        Gibberish Classifier
    '''
    # Pickle no 1: gibberish classifier
    with open('../budi_ml/feedback_nlp/gib_model.pickle', 'rb') as f:
        gibberish_clf = pickle.load(f)[0]
    gibberish_threshold = gibberish_clf['thresh']
    return not gib_detect_train.avg_transition_prob(sentence, gibberish_clf['mat']) > gibberish_threshold


'''
Naive Bayes Classifier
'''
# these 2 functions remove punctuation, lowercase, stem. They are used by
# the NB classifier

def stem_tokens(tokens):
    stemmer = nltk.stem.porter.PorterStemmer()
    return [stemmer.stem(item) for item in tokens]


def normalize(text):
    remove_punctuation_map = dict((ord(char), None) for char in string.punctuation)
    return stem_tokens(nltk.word_tokenize(text.lower().translate(remove_punctuation_map)))


def nb_train():
    '''
    READ FILE, TRANSFORM TRAINING DATA INTO TF-IDF
    '''
    report_df = pd.read_csv('data/final_training_set.csv')
    X = np.array(report_df['REPORT'])
    y = np.array(report_df['CATEGORY'])
    # Pickle no 2: tfidf_vectorizer
    tfidf_vectorizer = None
    try:
        with open('tfidf_vectorizer.pickle', 'rb') as f:
            # print 'tfidf_vectorizer is already there, just load it...'
            tfidf_vectorizer = pickle.load(f)[0]
    except:
        pass

    if not tfidf_vectorizer:
        tfidf_vectorizer = TfidfVectorizer(
            tokenizer=normalize, stop_words='english')
        with open('tfidf_vectorizer.pickle', 'wb') as f:
            pickle.dump([tfidf_vectorizer], f)

    X = tfidf_vectorizer.fit_transform(X)

    # Pickle no 3: Naive Bayes Classifier
    nb_clf = None
    try:
        with open('nb_clf.pickle', 'rb') as f:
            # print 'nb_clf, just load it...'
            nb_clf = pickle.load(f)[0]
    except:
        pass

    if not nb_clf:
        # print 'Training NB classifier using the trained tfidf_vectorizer '
        nb_clf = MultinomialNB().fit(X, y)
        with open('nb_clf.pickle', 'wb') as f:
            pickle.dump([nb_clf], f)
    return tfidf_vectorizer, nb_clf


def main():
    # Ali, just see this section on how to use the models
    try:
        report = sys.argv[1]
    except IndexError:
        raise Exception("Please input report with parantheses surrounding it.")

    # First pipeline
    gibberish = determine_gibberish(report)
    if not gibberish:
        # Second pipeline
        tfidf_vectorizer, nb_clf = nb_train()
        report = [str(report)]
        report = tfidf_vectorizer.transform(np.array(report))
        result = nb_clf.predict_proba(report)
    else:
        result = [[0.0, 0.0]]
    print result[0][1]


if __name__ == '__main__':
    main()

# -*- coding: utf-8 -*-
"""
Auxiliary functions for tutorial 4
"""
import numpy as np
import matplotlib.pyplot as plt

def bootstrap(samples,data):
    """generate replicas by bootstrapping"""
    train_sets = np.zeros((samples,data.shape[0],data.shape[1]))
    test_sets = []
    for sample in range(samples):
        ix = np.random.randint(data.shape[0],size=data.shape[0])        
        train_sets[sample,:] = data[ix,:]
        in_train = np.unique(ix)
        mask = np.ones(data.shape[0],dtype = bool)
        mask[in_train] = False
        test_sets.append(np.arange(data.shape[0])[mask])
    return train_sets,test_sets
        

def plot_svm_list(svms, Xs,Ys, fig_size=(13,8), limits=(-2,2,-2,2),
                  file_name='SVM.png',plot_margins=False,plot_alfas=False):
    """Auxiliary function to plot SVM classification in 
    two-dimensional data"""    
    
    plt.figure(figsize=fig_size, frameon=False)
    pxs = np.linspace(-2,2,300)
    pys = np.linspace(-2,2,300)
    pX,pY = np.meshgrid(pxs,pys)
    pZ = np.zeros((len(pxs),len(pys)))    
    
    for sv in svms:
        preds = sv.predict(np.c_[pX.ravel(),pY.ravel()]).reshape(pZ.shape)
        plt.contour(pX, pY, preds, [0], linewidths =1,
                    colors = 'k',alpha = 0.3, linestyles='solid')    
        if plot_margins:
            df = sv.decision_function(np.c_[pX.ravel(),pY.ravel()]).reshape(pZ.shape)
            plt.contour(pX, pY, df, [-1,1], linewidths =1,
                    colors = 'g',alpha = 1, linestyles='solid')    
        pZ = pZ + preds

    pZ = np.round(pZ/float(len(svms)))
    plt.contourf(pX, pY, pZ, [-1e9, 0, 1e9],
                 colors = ('b','r'), alpha=0.2)
    plt.contour(pX, pY, pZ, [0], linewidths =3, colors = 'k',
                linestyles='solid')
    
    if plot_alfas:
        support = sv.support_vectors_
        C = sv.get_params()['C']
        alfas = np.abs(sv.dual_coef_[0,:])
        mask = alfas<C
        plt.plot(support[:,0],support[:,1],'ow',markersize = 18)
        plt.plot(support[mask,0],support[mask,1],'ok',markersize = 18)
    
    plt.plot(Xs[Ys==1,0],Xs[Ys==1,1],'or',markersize = 12)
    plt.plot(Xs[Ys==0,0],Xs[Ys==0,1],'ob',markersize = 12)   
    
    plt.axis(limits)            
    plt.savefig(file_name,bbox_inches='tight', dpi=300) 
    plt.close()

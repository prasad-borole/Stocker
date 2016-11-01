library(forecast)
library(lmtest)

setwd("/home/prasad/Stocker")
djia <- read.csv(file="DJIA2009.csv",head=TRUE,sep = ",")
djia$Date <- as.Date(strptime(djia$Date, "%Y%m%d"))
plot(djia$Date,djia$AdjClose,pch=23, type="o", col="red")

######## Fill missing date values by previous values ###########
alldates <- data.frame(Date=seq.Date(min(djia$Date), max(djia$Date), by="day"))
dt <- merge(djia, alldates, by="Date", all=TRUE)
require(xts)

dt = na.locf(dt)
dt$Date <- as.Date(strptime(dt$Date, "%Y-%m-%d"))
dt$AdjClose=as.numeric(dt$AdjClose)

##########   Calculate z-Score  #########
for(i in 1:nrow(dt)){
  if(i>3 && i<nrow(dt)-2){
    if(isTRUE(dt$AdjClose[i]!=dt$AdjClose[i-1])) {
      dt$newzScore[i] <- (dt$AdjClose[i] - mean(dt$AdjClose[i-3]:dt$AdjClose[i+3]))/sd(dt$AdjClose[i-3]:dt$AdjClose[i+3])
      #print (dt$AdjClose[i]) #<- dat$x[i]^2
    }
    else{
      dt$newzScore[i] <- (dt$AdjClose[i] - mean(dt$AdjClose))/sd(dt$AdjClose)
    }
  }
  else{
    dt$newzScore[i] <- (dt$AdjClose[i] - mean(dt$AdjClose))/sd(dt$AdjClose)
  }
}
plot(dt$newzScore,pch=23, type="o", col="red")

############ Reduce dt to correct size ############
dt<-dt[dt$Date>'2009-06-10',]

par(mfrow=c(2,1))
################ READ SENTIMENT FILE ##############
#require(xlsx)
tweetSent <- read.csv("new286Combinations.csv")
tweetSent$Date <- as.Date(strptime(tweetSent$Date, "%Y-%m-%d"))
senti <- data.frame(tweetSent$Date)
names(senti) <- c("Date")

############# Initialize final file ###############
final<- data.frame()#"Sentiment",2,3,4,5,6)
names(final)<-c("Sentiment", "Lag-2", "Lag-3", "Lag-4", "Lag-5", "Lag-6")


for(i in 2:ncol(tweetSent)){
  senti$score <- tweetSent[,i]
  colname <- colnames(tweetSent)[i]
  #if(isTRUE(colname=="X.vigour..neg_depression.")) { 
   # print (i) 
  #  }
#}
  for(j in 1:nrow(senti)) {
    if(j>3 && j<nrow(senti)-2){
      senti$z_score[j] <- (senti$score[j] - mean(senti$score[j-3]:senti$score[j+3]))/sd(senti$score, na.rm = TRUE)
    }
    else{
      senti$z_score[j] <- (senti$score[j] - mean(senti$score))/sd(senti$score)
    }
  }

  ################ GRANGER TEST #############
  sentdf<-senti$z_score
  sent <- diff(sentdf)

  newdtdf <- dt$newzScore 
  dj <- diff(newdtdf)
  
  #plot.ts(sent)
  #plot.ts(dj)
  final[i,1]=as.factor(paste(colnames(tweetSent)[i]))
  for(k in 2:6){
    result<-grangertest(dj ~ sent, order=k)
    final[i,k]<- result$`Pr(>F)`[2]
  }
  
  
}

########### RESULT PROCESSING #########
x<-final$V2
n <- length(x)
sort(x,partial=n-1)[n-1]

for(j in 1:nrow(tweetSent)) {
  if(j>3 && j<nrow(tweetSent)-2){
    senti$z_score[j] <- (tweetSent$X.vigour..neg_depression.[j] - mean(tweetSent$X.vigour..neg_depression.[j-3]:tweetSent$X.vigour..neg_depression.[j+3]))/sd(tweetSent$X.vigour..neg_depression., na.rm = TRUE)
  }
  else{
    senti$z_score[j] <- (tweetSent$X.vigour..neg_depression.[j] - mean(tweetSent$X.vigour..neg_depression.))/sd(tweetSent$X.vigour..neg_depression.)
  }
}

s<-senti[senti$Date<'2009-07-16',]
s<-s[s$Date>'2009-06-14',]
nd<-dt[dt$Date<'2009-07-16',]
nd<-nd[nd$Date>'2009-06-14',]
s$Date<-s$Date-2
p<-ggplot() + 
  geom_line(data = s, aes(Date, z_score, group = 1), color="green") +
  #geom_line(data = sentiment1, aes(Date, z_depression, group = 1), color="blue") +
  geom_line(data = nd, aes(Date, newzScore, group = 1) , color="black") + 
  xlab("Date") + ylab("z-score") 
p+ggtitle("Vigour+neg of Depression vs Stock Variation(2009)")

########### Learning Models Linear Regrerssion ###########
zscore=dt[dt$Date<'2009-11-01',]$newzScore
sent_lm<-senti[senti$Date<'2009-11-01',]$z_score
model = lm(zscore ~ sent_lm)

pred<-model$coefficients[1]+model$coefficients[2]*senti[senti$Date>'2009-10-30',]$z_score

#pred <- predict(model, zscore=senti[senti$Date>'2009-10-30',]$z_score)

plot(pred,type="o", col="blue")
lines(zscore, type="o", pch=22, lty=2, col="red")
newd=dt
p<-ggplot() + 
  geom_line(data = s, aes(Date, z_score, group = 1), color="green") +
  #geom_line(data = sentiment1, aes(Date, z_depression, group = 1), color="blue") +
  geom_line(data = dt[dt$Date<'2009-08-1',], aes(Date, newzScore, group = 1) , color="black") + 
  geom_line(data = pred, aes(pred, group = 1) , color="blue") + 
  xlab("Date") + ylab("z-score") 
p+ggtitle("Pred vs Stock Variation(2009)")


################### TEST ##################
for(j in 1:nrow(tweetSent)) {
  if(j>3 && j<nrow(tweetSent)-2){
    senti$z_score[j] <- (tweetSent$X.anger..confusion..tension.[j] - mean(tweetSent$X.anger..confusion..tension.[j-3]:tweetSent$X.anger..confusion..tension.[j+3]))/sd(tweetSent$X.anger..confusion..tension., na.rm = TRUE)
  }
  else{
    senti$z_score[j] <- (tweetSent$X.anger..confusion..tension.[j] - mean(tweetSent$X.anger..confusion..tension.))/sd(tweetSent$X.anger..confusion..tension.)
  }
}

s<-senti[senti$Date<'2009-07-21',]
s<-s[s$Date>'2009-07-9',]
nd<-dt[dt$Date<'2009-07-21',]
nd<-nd[nd$Date>'2009-07-9',]
s$Date<-s$Date-2
p<-ggplot() + 
  geom_line(data = s, aes(Date, z_score, group = 1), color="green") +
  #geom_line(data = sentiment1, aes(Date, z_depression, group = 1), color="blue") +
  geom_line(data = nd, aes(Date, newzScore, group = 1) , color="black") + 
  xlab("Date") + ylab("z-score") 
p+ggtitle("ACT vs Stock Variation(2009)")


######## Calculate directional Accuracy ######
ndd = dt[dt$Date>'2009-10-30',]
flag=0
count=0
op_pred={0.1}
op_act={0.1}
num=0
denom=0
for(i in 2:length(pred)) {
  if(pred[i]>pred[i-1]){
    flag=1
    op_pred[i]=op_pred[i-1]+0.05
  }
  else{
    flag=-1
    op_pred[i]=op_pred[i-1]-0.05
  }
  if(ndd$newzScore[i]>ndd$newzScore[i-1]){
    if(isTRUE(flag==1))
      count=count+1
    op_act[i]=op_act[i-1]+0.05
  }
  else {
    if(isTRUE(flag==-1))
      count=count+1
    op_act[i]=op_act[i-1]-0.05
  }
  flag=0
  if(isTRUE(op_act[i]>0.005))
    if(isTRUE(op_act[i]<-0.005))
      num=num + abs((op_act[i] - op_pred[i])/(op_act[i]))
  
  print (num)
  print (i)
  
}
print (count/length(pred)*100) #Accuracy
print(num/length(pred))#Error

plot(op_act[1:50],type="o", col="blue",ann=FALSE)
lines(op_pred[1:50], type="o", pch=22, lty=2, col="red",ann=FALSE)
title(main="Actual(Blue) vs Predicted(Red)", col.main="red", font.main=4)
title(xlab="Days", col.lab=rgb(0,0.5,0))
title(ylab="Normalized z-score", col.lab=rgb(0,0.5,0))


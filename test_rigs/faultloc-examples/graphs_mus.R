args = commandArgs(trailingOnly=TRUE)

mydata = read.csv(args[1],header=TRUE,sep=",")

mydata$percentfound<-ifelse(mydata$found_preds_count+mydata$missed_preds_count>0,(mydata$found_preds_count/(mydata$found_preds_count+mydata$missed_preds_count))*100,100)

mydata$precision<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$extra_count))*100
mydata[is.na(mydata)]=0

print (args[1])
print (mean(mydata$percentfound))
print (mean(mydata$precision))
id = substr(args[1], 12, 14)

colors <- c('red', 'yellow', 'green', 'blue', 'purple', 'orange')

png(paste("extra(network)_", id,".png",sep=""),width = 800)
boxplot (mydata$extra_count~mydata$network, main="Number of extra config predicates", xlab="network", ylab="number", ylim=c(0, 150))
dev.off()

png(paste("extra(scenario)_" ,id, ".png",sep=""),width = 800)
boxplot (mydata$extra_count~mydata$scenario, main="Number of extra config predicates", xlab="scenario", ylab="number", ylim=c(0, 150))
dev.off()

png(paste("recall(network)_", id, ".png", sep=""),width = 800)
plotdata <- table(mydata$percentfound, mydata$network)
barplot (plotdata, main="Recall", xlab="Network", ylab="Number of violations", col=colors, ylim=c(0,10))
legend(0, 10, fill=colors, legend=rownames(plotdata))
dev.off()

png(paste("precision(network)_", id ,".png",sep=""), width = 800)
boxplot(mydata$precision~mydata$network, main="Precision by network", xlab="network", ylab="precision",ylim=c(0,100))
dev.off()

png(paste("precision(scenario)_", id , ".png",sep=""), width = 800)
boxplot(mydata$precision~mydata$scenario, main="Precision by scenario", xlab="scenario", ylab="precision",ylim=c(0,100))
dev.off()

png(paste("recall(scenario)_", id ,".png",sep=""),width = 800)
plotdata <- table(mydata$percentfound, mydata$scenario)
barplot (plotdata, main="Recall", xlab="Scenario", ylab="Number of violations", col=colors, ylim=c(0,10))
legend(0, 10, fill=colors, legend=rownames(plotdata))
dev.off()

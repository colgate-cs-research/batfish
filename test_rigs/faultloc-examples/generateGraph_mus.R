mydata = read.csv("master_mus.csv",header=TRUE,sep=",")
mydata$percentfound<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$missed_preds_count))*100
mydata$precision<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$extra_count))*100
mydata[is.na(mydata)]=0

colors <- c('red', 'yellow', 'green')

png("extra(network).png",width = 800)
boxplot (mydata$extra_count~mydata$network, main="Number of extra config predicates", xlab="network", ylab="number", ylim=c(0, 150))
dev.off()

png("extra(scenario).png",width = 800)
boxplot (mydata$extra_count~mydata$scenario, main="Number of extra config predicates", xlab="scenario", ylab="number", ylim=c(0, 150))
dev.off()

png("recall(network).png",width = 800)
plotdata <- table(mydata$percentfound, mydata$network)
barplot (plotdata, main="Recall", xlab="Network", ylab="Number of violations", col=colors, ylim=c(0,25))
legend(0, 100, fill=colors, legend=rownames(plotdata))
dev.off()

png("precision(network).png", width = 800)
boxplot(mydata$precision~mydata$network, main="Precision by network", xlab="network", ylab="precision",ylim=c(0,100))
dev.off()

png("precision(scenario).png", width = 800)
boxplot(mydata$precision~mydata$scenario, main="Precision by scenario", xlab="scenario", ylab="precision",ylim=c(0,100))
dev.off()

png("recall(scenario).png",width = 800)
plotdata <- table(mydata$percentfound, mydata$scenario)
barplot (plotdata, main="Recall", xlab="Scenario", ylab="Number of violations", col=colors, ylim=c(0,100))
legend(0, 100, fill=colors, legend=rownames(plotdata))
dev.off()

png("allMUSes_genTime(scenario).png",width = 800)
boxplot (mydata$allMUSes_genTime~mydata$scenario, main="all MUSes genTime time", xlab="scenario", ylab="miliseconds")
dev.off()

png("allMUSes_genTime(network).png",width = 800)
boxplot (mydata$allMUSes_genTime~mydata$network, main="all MUSes genTime time", xlab="scenario", ylab="miliseconds")
dev.off()

for (exp in unique(mydata$experiment)){
  expdata <- subset(mydata, experiment==exp)

  png(paste("found", exp, "scenario.png", sep="-"),width = 800)
  plotdata <- table(expdata$percentfound, expdata$scenario)
  ymax <- max(colSums(plotdata))
  barplot (plotdata, main=paste("Percent of predicates found using", exp),
           xlab="Scenario", ylab="Number of violations", col=colors,
           ylim=c(0,ymax*1.1))
  legend(0, ymax, fill=colors, legend=rownames(plotdata))
  dev.off()

  png(paste("found", exp, "network.png", sep="-"),width = 800)
  plotdata <- table(expdata$percentfound, expdata$network)
  ymax <- max(colSums(plotdata))
  barplot (plotdata, main=paste("Percent of predicates found using", exp),
           xlab="Network", ylab="Number of violations", col=colors,
           ylim=c(0,ymax*1.1))
  legend(0, ymax, fill=colors, legend=rownames(plotdata))
  dev.off()
}

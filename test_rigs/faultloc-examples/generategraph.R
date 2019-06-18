mydata = read.csv("master.csv",header=TRUE,sep=",")
mydata$percentfound<-ifelse(mydata$foundpreds+mydata$missedpreds>0,(mydata$foundpreds/(mydata$foundpreds+mydata$missedpreds))*100,100)
mydata$precision<-(mydata$foundpreds/(mydata$foundpreds+mydata$extraconfigpred))*100
mydata[is.na(mydata)]=0

colors <- c('red', 'yellow', 'green','blue', 'black', 'purple')

#png("percent.png",width = 800)
#boxplot (mydata$percentfound~mydata$experiment, main="Percent of predicates found", xlab="Experiment", ylab="Percent")
#dev.off()

png("extra(network).png",width = 800)
boxplot (mydata$extraconfigpred~mydata$network, main="Number of extra config predicates", xlab="network", ylab="number", ylim=c(0, 150))
dev.off()

png("extra(scenario).png",width = 800)
boxplot (mydata$extraconfigpred~mydata$scenario, main="Number of extra config predicates", xlab="scenario", ylab="number", ylim=c(0, 150))
dev.off()

png("recall(network).png",width = 800)
plotdata <- table(mydata$percentfound, mydata$network)
barplot (plotdata, main="Recall", xlab="Network", ylab="Number of violations", col=colors, ylim=c(0,25))
legend(0, 25, fill=colors, legend=rownames(plotdata))
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

png("firstCEGenTime(scenario).png",width = 800)
boxplot (mydata$firstCE_genTime~mydata$scenario, main="first CE genTime", xlab="scenario", ylab="miliseconds")
dev.off()

png("firstCEGenTime(network).png",width = 800)
boxplot (mydata$firstCE_genTime~mydata$network, main="first CE genTime", xlab="network", ylab="miliseconds")
dev.off()

png("allCEGenTime(network).png",width = 800)
boxplot (mydata$allCEs_genTime~mydata$network, main="all CEs genTime", xlab="network", ylab="miliseconds")
dev.off()

png("allCEGenTime(scenario).png", width = 800)
boxplot (mydata$allCEs_genTime~mydata$scenario, main="all CEs genTime", xlab="scenario", ylab="milliseconds")
dev.off()

png("firstMUSGenTime(network).png",width = 800)
boxplot (mydata$firstMUS_genTime~mydata$network, main="first MUS genTime", xlab="network", ylab="miliseconds")
dev.off()

png("firstFailSetMUSes_genTime(network).png",width = 800)
boxplot (mydata$firstFailSetMUSes_genTime~mydata$network, main="first FailSet MUSes genTime", xlab="Network", ylab="miliseconds")
dev.off()

png("firstFailSetMUSes_genTime(scenario).png",width = 800)
boxplot (mydata$firstFailSetMUSes_genTime~mydata$scenario, main="first FailSet MUSes genTime", xlab="scenario", ylab="miliseconds", outline=FALSE)
dev.off()

png("allMUSes_genTime(scenario).png",width = 800)
boxplot (mydata$allMUSes_genTime~mydata$scenario, main="all MUSes genTime time", xlab="scenario", ylab="miliseconds")
dev.off()

# for (exp in unique(mydata$experiment)){
#   temp<-subset(mydata,experiment==exp)
#   png(paste("foundpred-", exp, ".png"),width = 800)
#   boxplot (temp$percentfound~temp$scenario, main="percent of foundpred", xlab="different network", ylab="percentage")
#   dev.off()
#   png(paste("foundpred(", exp, ",network).png"),width = 800)
#   boxplot (temp$percentfound~temp$network, main="percent of foundpred", xlab="different network", ylab="percentage")
#   dev.off()
# }
  
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

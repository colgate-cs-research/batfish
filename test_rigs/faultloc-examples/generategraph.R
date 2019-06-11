mydata = read.csv("master.csv",header=TRUE,sep=",")
mydata$percentfound<-(mydata$foundpreds/(mydata$foundpreds+mydata$missedpreds))*100
mydata[is.na(mydata)]=0

colors <- c('red', 'yellow', 'green')

#png("percent.png",width = 800)
#boxplot (mydata$percentfound~mydata$experiment, main="Percent of predicates found", xlab="Experiment", ylab="Percent")
#dev.off()

png("extra.png",width = 800)
boxplot (mydata$extraconfigpred~mydata$experiment, main="Number of extra config predicates", xlab="Experiment", ylab="number", ylim=c(0, 250))
dev.off()

png("found.png",width = 800)
plotdata <- table(mydata$percentfound, mydata$experiment)
ymax <- max(colSums(plotdata))
barplot (plotdata, main="Percent of predicates found", xlab="Experiment", ylab="Number of violations", col=colors, ylim=c(0,ymax*1.1))
legend(0, ymax, fill=colors, legend=rownames(plotdata))
dev.off()

png("firstMUSGenTime(network).png",width = 800)
boxplot (mydata$firstMUS_genTime~mydata$network, main="first MUS genTime", xlab="network", ylab="miliseconds")
dev.off()

png("firstMUSGenTime(experiment).png",width = 800)
boxplot (mydata$firstMUS_genTime~mydata$experiment, main="first MUS genTime", xlab="experiment", ylab="miliseconds", outline=FALSE)
dev.off()

png("firstFailSetMUSes_genTime(experiment).png",width = 800)
boxplot (mydata$firstFailSetMUSes_genTime~mydata$experiment, main="first FailSet MUSes genTime", xlab="experiment", ylab="miliseconds")
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

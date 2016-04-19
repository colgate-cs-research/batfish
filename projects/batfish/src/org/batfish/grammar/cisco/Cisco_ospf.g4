parser grammar Cisco_ospf;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

area_ipv6_ro_stanza
:
   AREA ~NEWLINE* NEWLINE
;

area_nssa_ro_stanza
:
   AREA
   (
      area_int = DEC
      | area_ip = IP_ADDRESS
   ) NSSA
   (
      NO_REDISTRIBUTION
      | NO_SUMMARY
      | DEFAULT_INFORMATION_ORIGINATE
      | METRIC_TYPE DEC
   )* NEWLINE
;

area_ro_stanza
:
	AREA
	(
		area_int = DEC
		| area_ip = IP_ADDRESS
	)
	(
		DEFAULT_COST DEC
		| NO_SUMMARY
		| NOT_ADVERTISE
		| RANGE IP_ADDRESS IP_ADDRESS
		| STUB
	)* NEWLINE
;

auto_cost_ipv6_ro_stanza
:
   AUTO_COST REFERENCE_BANDWIDTH DEC NEWLINE
;

default_information_ipv6_ro_stanza
:
   DEFAULT_INFORMATION ~NEWLINE* NEWLINE
;

default_information_ro_stanza
:
   DEFAULT_INFORMATION ORIGINATE
   (
      (
         METRIC metric = DEC
      )
      |
      (
         METRIC_TYPE metric_type = DEC
      )
      | ALWAYS
      |
      (
         ROUTE_MAP map = VARIABLE
      )
   )* NEWLINE
;

distance_ipv6_ro_stanza
:
   DISTANCE value = DEC NEWLINE
;

distance_ro_stanza
:
   DISTANCE value = DEC NEWLINE
;

ipv6_ro_stanza
:
   area_ipv6_ro_stanza
   | auto_cost_ipv6_ro_stanza
   | default_information_ipv6_ro_stanza
   | distance_ipv6_ro_stanza
   | log_adjacency_changes_ipv6_ro_stanza
   | maximum_paths_ipv6_ro_stanza
   | passive_interface_ipv6_ro_stanza
   | redistribute_ipv6_ro_stanza
   | router_id_ipv6_ro_stanza
;

ipv6_router_ospf_stanza
:
   IPV6 ROUTER OSPF procnum = DEC NEWLINE
   (
      rosl += ipv6_ro_stanza
   )+
;

log_adjacency_changes_ipv6_ro_stanza
:
   LOG_ADJACENCY_CHANGES NEWLINE
;

maximum_paths_ipv6_ro_stanza
:
   MAXIMUM_PATHS DEC NEWLINE
;

maximum_paths_ro_stanza
:
   MAXIMUM_PATHS DEC NEWLINE
;

network_ro_stanza
:
   NETWORK ip = IP_ADDRESS wildcard = IP_ADDRESS AREA
   (
      area_int = DEC
      | area_ip = IP_ADDRESS
   ) NEWLINE
;

null_ro_stanza
:
   null_standalone_ro_stanza
;

null_standalone_ro_stanza
:
   NO?
   (
      (
         AREA
         (
            DEC
            | IP_ADDRESS
         ) AUTHENTICATION
      )
      | AUTO_COST
      | BFD
      | CAPABILITY
      | DISTRIBUTE_LIST
      | LOG_ADJACENCY_CHANGES
      | MAX_METRIC
      | NSF
   ) ~NEWLINE* NEWLINE
;

passive_interface_ipv6_ro_stanza
:
   NO? PASSIVE_INTERFACE ~NEWLINE* NEWLINE
;

passive_interface_default_ro_stanza
:
   PASSIVE_INTERFACE DEFAULT NEWLINE
;

passive_interface_ro_stanza
:
   NO? PASSIVE_INTERFACE i += VARIABLE
   (
      i += COLON DEC
   )?
   NEWLINE
;

redistribute_bgp_ro_stanza
:
   REDISTRIBUTE BGP as = DEC
   (
      (
         METRIC metric = DEC
      )
      |
      (
         METRIC_TYPE type = DEC
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
      | subnets = SUBNETS
      |
      (
         TAG tag = DEC
      )
   )* NEWLINE
;

redistribute_ipv6_ro_stanza
:
   REDISTRIBUTE ~NEWLINE* NEWLINE
;

redistribute_connected_ro_stanza
:
   REDISTRIBUTE CONNECTED
   (
      (
         METRIC metric = DEC
      )
      |
      (
         METRIC_TYPE type = DEC
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
      | subnets = SUBNETS
      |
      (
         TAG tag = DEC
      )
   )* NEWLINE
;

redistribute_ospf_ro_stanza
:
   REDISTRIBUTE OSPF
   (
      (
      	 procnum = DEC
      )
      |
      (
         METRIC metric = DEC
      )
      |
      (
         METRIC_TYPE type = DEC
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
      | subnets = SUBNETS
      |
      (
         TAG tag = DEC
      )
   )*
   NEWLINE
;

redistribute_rip_ro_stanza
:
   REDISTRIBUTE RIP ~NEWLINE* NEWLINE
;

redistribute_static_ro_stanza
:
   REDISTRIBUTE STATIC
   (
      (
         METRIC metric = DEC
      )
      |
      (
         METRIC_TYPE type = DEC
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
      | subnets = SUBNETS
      |
      (
         TAG tag = DEC
      )
   )* NEWLINE
;

ro_stanza
:
   area_nssa_ro_stanza
   | area_ro_stanza
   | default_information_ro_stanza
   | distance_ro_stanza
   | maximum_paths_ro_stanza
   | network_ro_stanza
   | null_ro_stanza
   | passive_interface_default_ro_stanza
   | passive_interface_ro_stanza
   | redistribute_bgp_ro_stanza
   | redistribute_connected_ro_stanza
   | redistribute_ospf_ro_stanza
   | redistribute_rip_ro_stanza
   | redistribute_static_ro_stanza
   | router_id_ro_stanza
   | summary_address_ro_stanza
;

router_id_ipv6_ro_stanza
:
   ROUTER_ID ~NEWLINE* NEWLINE
;

router_id_ro_stanza
:
   ROUTER_ID ip = IP_ADDRESS NEWLINE
;

router_ospf_stanza
:
   ROUTER OSPF procnum = DEC
   (
      VRF vrf = variable
   )? NEWLINE router_ospf_stanza_tail
;

router_ospf_stanza_tail
:
   (
      rosl += ro_stanza
   )+
;

summary_address_ro_stanza
:
   SUMMARY_ADDRESS network = IP_ADDRESS mask = IP_ADDRESS NOT_ADVERTISE?
   NEWLINE
;

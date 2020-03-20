parser grammar CiscoNxos_flow;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_flow
:
  FLOW flow_exporter
;

flow_exporter
:
  EXPORTER fe_name NEWLINE 
  (
    fe_null
  )*
;

fe_name
:
  WORD
;

fe_null
:
  (
    DESTINATION
    | SOURCE
    | TRANSPORT
    | VERSION
  ) null_rest_of_line
;

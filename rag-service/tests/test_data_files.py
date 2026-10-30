"""Checks the real sources.json, contacts.json and routing prompt agree on the department domains.

A domain missing from contacts.json would leave the assistant with no team to hand off to, and a
routing rule naming an unknown domain would make the retriever filter on something that never matches.
"""

import re

from app.chains import DOMAIN_ROUTING_RULES
from app.data import load_contacts, load_sources_config


def test_every_source_domain_has_a_contact():
    contacts = load_contacts()
    domains = {source["metadata"]["domain"] for source in load_sources_config()["sources"]}

    assert domains <= contacts.keys()


def test_every_routing_rule_names_a_known_domain():
    routed = set(re.findall(r"use '(\w+)'", DOMAIN_ROUTING_RULES))

    assert routed  # the pattern still matches the rules' wording
    assert routed <= load_contacts().keys()

# Task 4 Report

Scope: advanced machine settings copier compatibility and six-sided auto-I/O configuration.

## Fix

- Stores the exact block-entity type ID together with copied settings.
- Refuses to paste when the source and target machine types differ, including Clicker to Placer.
- Preserves the original JDT copier settings through delegated base save/load hooks.
- Copies and restores both six-side input/output masks for machines exposing JDTE auto-I/O.
- Rejects legacy copied data without a machine-type fingerprint instead of applying it to an unrelated machine.

## Verification

```text
.\gradlew.bat test --tests com.jdte.common.items.AdvancedMachineSettingsCopierDataTest --tests com.jdte.common.items.AdvancedMachineSettingsCopierItemTest --no-daemon
BUILD SUCCESSFUL in 43s
```

1. Double check documentation is up to date.
2. Validate version in build.sbt
3. Validate entry in CHANGELOG.md
4. Run `sbt +clean` and `sbt +test`
5. Set tag `git tag version-3.5.3`
6. `./crossrelease.sh`
7. `git push --tags`
8. Login to Sonatype's Nexus, close and release the staging repository.
9. Update version in `README.md`
10. Add version in `docs/AvailableVersions.md`
11. Check the documented links online.

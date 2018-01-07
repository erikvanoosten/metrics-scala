# Check list for 4.x

1. Double check documentation is up to date.
2. Validate entry in CHANGELOG.md
4. Run `sbt ';+clean;+test;+package`
5. Push changes, e.g.`git commit -m 'Releasing 4.x.x'`
6. Set tag `git tag v3.x.x`
6. Optionally test signed publishing with `./crossrelease.sh publishLocalSigned` 
7. `./crossrelease.sh`
8. `git push` && `git push --tags`
9. Login to Sonatype's Nexus, close and release the staging repository.
10. Update version in `README.md`
11. Add version in `docs/AvailableVersions.md` and push again
12. Check the documented links online.
13. Send e-mail to metrics user list.
14. Tweet.

# Check list for 2.x and 3.x. 

1. Double check documentation is up to date.
2. Validate version in build.sbt
3. Validate entry in CHANGELOG.md
4. Run `./crossrelease.sh clean` and `./crossrelease.sh test`
5. Push changes, e.g.`git commit -m 'Releasing 3.x.x'`
6. Set tag `git tag version-3.x.x`
7. `./crossrelease.sh`
8. `git push` && `git push --tags`
9. Change version in build.sbt, append `-snapshot`.
10. Login to Sonatype's Nexus, close and release the staging repository.
11. Update version in `README.md`
12. Add version in `docs/AvailableVersions.md` and push again
13. Check the documented links online.
14. Send e-mail to metrics user list.
15. Tweet.
